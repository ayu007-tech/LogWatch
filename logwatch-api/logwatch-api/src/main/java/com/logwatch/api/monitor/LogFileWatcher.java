package com.logwatch.api.monitor;

import com.logwatch.api.model.LogEntry;
import com.logwatch.api.service.LogService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogFileWatcher implements ApplicationRunner {

    private final LogService logService;

    @Value("${logwatch.file:../../logs/application.log}")
    private String configuredFile;

    @Value("${logwatch.checkpoint:../../logs/application.log.position}")
    private String configuredCheckpoint;

    private static final Pattern LOG_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})"
        + "\\s+(INFO|WARNING|ERROR|EXCEPTION)\\s+(.+)$",
        Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogFileWatcher(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path file = Path.of(configuredFile).toAbsolutePath().normalize();
        Path checkpoint =
            Path.of(configuredCheckpoint).toAbsolutePath().normalize();

        Path directory = file.getParent();
        Files.createDirectories(directory);
        Files.createDirectories(checkpoint.getParent());

        if (Files.notExists(file)) {
            Files.createFile(file);
        }

        WatchService watchService =
            FileSystems.getDefault().newWatchService();

        directory.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY
        );

        Thread thread = new Thread(
            () -> watchFile(file, checkpoint, watchService),
            "logwatch-file-watcher"
        );

        thread.setDaemon(true);
        thread.start();

        System.out.println("[LogWatch] Monitoring file: " + file);
        System.out.println("[LogWatch] Checkpoint: " + checkpoint);
    }

    private void watchFile(
            Path file,
            Path checkpoint,
            WatchService watchService) {

        String fileName = file.getFileName().toString();

        try (RandomAccessFile reader =
                 new RandomAccessFile(file.toFile(), "r")) {

            StringBuilder pendingLine = new StringBuilder();
            long position;

            if (Files.exists(checkpoint)) {
                try {
                    position = Long.parseLong(
                        Files.readString(checkpoint).trim()
                    );

                    if (position < 0 || position > reader.length()) {
                        position = 0;
                    }

                    System.out.println(
                        "[LogWatch] Resuming from byte " + position
                    );
                } catch (Exception e) {
                    position = reader.length();
                    saveCheckpoint(checkpoint, position);

                    System.out.println(
                        "[LogWatch] Invalid checkpoint; starting at file end."
                    );
                }
            } else {
                // First run: don't re-ingest historical file contents.
                position = reader.length();
                saveCheckpoint(checkpoint, position);

                System.out.println(
                    "[LogWatch] Initial checkpoint set to byte " + position
                );
            }

            reader.seek(position);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Object context = event.context();

                    if (context instanceof Path changed
                            && changed.getFileName().toString()
                                .equals(fileName)) {

                        if (Files.exists(file)
                                && Files.size(file) < position) {
                            // File was truncated.
                            position = 0;
                            pendingLine.setLength(0);
                            reader.seek(0);
                            saveCheckpoint(checkpoint, 0);
                        }

                        position = readNewContent(
                            reader,
                            position,
                            pendingLine,
                            checkpoint
                        );
                    }
                }

                if (!key.reset()) {
                    System.err.println(
                        "[LogWatch] File watch directory is unavailable."
                    );
                    break;
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println(
                "[LogWatch] Watcher failed: " + e.getMessage()
            );
            e.printStackTrace();
        } finally {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
        }
    }

    private long readNewContent(
            RandomAccessFile reader,
            long position,
            StringBuilder pendingLine,
            Path checkpoint) throws IOException {

        reader.seek(position);

        int value;

        while ((value = reader.read()) != -1) {
            if (value == '\n') {
                String line = pendingLine.toString();

                if (line.endsWith("\r")) {
                    line = line.substring(0, line.length() - 1);
                }

                pendingLine.setLength(0);
                processLine(line);

                // Save progress only after a complete line.
                saveCheckpoint(checkpoint, reader.getFilePointer());
            } else {
                pendingLine.append((char) (value & 0xFF));
            }
        }

        return reader.getFilePointer();
    }

    private void saveCheckpoint(Path checkpoint, long position) {
        try {
            Files.writeString(
                checkpoint,
                Long.toString(position),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            System.err.println(
                "[LogWatch] Could not save checkpoint: " + e.getMessage()
            );
        }
    }

    private void processLine(String rawLine) {
        String line = rawLine.trim();

        if (line.isEmpty()) {
            return;
        }

        Matcher matcher = LOG_PATTERN.matcher(line);

        if (!matcher.matches()) {
            System.err.println(
                "[LogWatch] Skipping unrecognized line: " + line
            );
            return;
        }

        try {
            String timestamp = matcher.group(1);
            String level = matcher.group(2).toUpperCase(Locale.ROOT);
            String message = matcher.group(3);

            LocalDateTime.parse(timestamp, TIMESTAMP_FORMAT);

            LogEntry entry = new LogEntry(
                null,
                timestamp,
                level,
                message
            );

            logService.addLog(entry);

            System.out.println(
                "[LogWatch] Ingested " + level + ": " + message
            );

        } catch (Exception e) {
            System.err.println(
                "[LogWatch] Could not process line: "
                + line + " (" + e.getMessage() + ")"
            );
        }
    }
}
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class LogMonitor {

    private final String filePath;
    private final LogParser parser;
    private final LogStatistics statistics;
    private final ErrorAnalyzer errorAnalyzer;
    private final AlertManager alertManager;
    private final HealthAnalyzer healthAnalyzer;

    private int processedLines = 0;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public LogMonitor(
            String filePath,
            LogParser parser,
            LogStatistics statistics,
            ErrorAnalyzer errorAnalyzer,
            AlertManager alertManager,
            HealthAnalyzer healthAnalyzer) {

        this.filePath = filePath;
        this.parser = parser;
        this.statistics = statistics;
        this.errorAnalyzer = errorAnalyzer;
        this.alertManager = alertManager;
        this.healthAnalyzer = healthAnalyzer;
    }


    // =========================================================
    // PROCESS EXISTING LOGS
    // =========================================================

    public void processExistingLogs() {

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                LogEntry logEntry =
                        parser.parse(line);

                if (logEntry != null) {

                    statistics.addLog(logEntry);

                    errorAnalyzer.analyze(logEntry);

                    processedLines++;
                }
            }

            System.out.println(
                    "Existing logs processed: "
                            + processedLines
            );

        } catch (IOException e) {

            System.out.println(
                    "Error reading log file: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // START REAL-TIME MONITORING
    // =========================================================

    public void startMonitoring() {

        try {

            Path path =
                    Path.of(filePath).toAbsolutePath();

            Path directory =
                    path.getParent();

            String fileName =
                    path.getFileName().toString();


            WatchService watchService =
                    FileSystems.getDefault()
                            .newWatchService();


            directory.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );


            System.out.println();

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "     LOGWATCH REAL-TIME MONITOR"
            );

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "Monitoring file: "
                            + fileName
            );

            System.out.println(
                    "Status: ACTIVE"
            );

            System.out.println(
                    "Waiting for new logs..."
            );

            System.out.println(
                    "Press Ctrl + C to stop."
            );


            // =================================================
            // CONTINUOUS MONITORING
            // =================================================

            while (true) {

                WatchKey key =
                        watchService.take();


                for (WatchEvent<?> event :
                        key.pollEvents()) {

                    if (event.kind()
                            == StandardWatchEventKinds.OVERFLOW) {

                        continue;
                    }


                    Path changedFile =
                            (Path) event.context();


                    if (changedFile
                            .getFileName()
                            .toString()
                            .equals(fileName)) {

                        processNewLogs();
                    }
                }


                // Reset watch key
                if (!key.reset()) {

                    System.out.println(
                            "Watch service stopped."
                    );

                    break;
                }
            }


        } catch (IOException e) {

            System.out.println(
                    "Monitoring error: "
                            + e.getMessage()
            );


        } catch (InterruptedException e) {

            System.out.println(
                    "Monitoring interrupted."
            );

            Thread.currentThread().interrupt();
        }
    }


    // =========================================================
    // PROCESS NEW LOGS
    // =========================================================

    private void processNewLogs() {

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;

            int currentLine = 0;

            boolean newLogFound = false;


            while ((line = reader.readLine()) != null) {

                currentLine++;


                // Skip already processed logs
                if (currentLine <= processedLines) {

                    continue;
                }


                LogEntry logEntry =
                        parser.parse(line);


                if (logEntry != null) {

                    newLogFound = true;


                    System.out.println();

                    System.out.println(
                            "========== NEW LOG DETECTED =========="
                    );

                    System.out.println(
                            logEntry
                    );

                    System.out.println(
                            "======================================"
                    );


                    // Update statistics
                    statistics.addLog(logEntry);


                    // Analyze errors
                    errorAnalyzer.analyze(logEntry);


                    // Mark as processed
                    processedLines++;
                }
            }


            // =================================================
            // UPDATE DASHBOARD ONLY IF NEW LOG FOUND
            // =================================================

            if (newLogFound) {

                statistics.displayStatistics();

                errorAnalyzer.displayErrorAnalysis();

                healthAnalyzer.displayHealth(
                        statistics
                );

                alertManager.checkAlerts(
                        statistics
                );
            }


        } catch (IOException e) {

            System.out.println(
                    "Error processing new logs: "
                            + e.getMessage()
            );
        }
    }
}
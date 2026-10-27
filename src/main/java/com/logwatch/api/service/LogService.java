```java
package com.logwatch.api.service;

import com.logwatch.api.model.LogEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class LogService {

    // Stores logs in memory
    private final List<LogEntry> logs =
            new CopyOnWriteArrayList<>();

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    // =========================================================
    // ADD SINGLE LOG
    // =========================================================

    public LogEntry addLog(LogEntry log) {

        if (log == null) {
            throw new IllegalArgumentException(
                    "Log cannot be null"
            );
        }

        String level = log.getLevel();
        String message = log.getMessage();

        if (level == null || level.isBlank()) {
            throw new IllegalArgumentException(
                    "Log level is required"
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Log message is required"
            );
        }

        level = level.trim().toUpperCase();

        // Allowed levels
        if (!Set.of(
                "INFO",
                "WARNING",
                "ERROR",
                "EXCEPTION"
        ).contains(level)) {

            throw new IllegalArgumentException(
                    "Invalid log level. Use INFO, WARNING, ERROR or EXCEPTION"
            );
        }

        String timestamp = log.getTimestamp();

        // Generate timestamp automatically
        if (timestamp == null || timestamp.isBlank()) {

            timestamp =
                    LocalDateTime.now()
                            .format(formatter);
        }

        LogEntry savedLog = new LogEntry(
                UUID.randomUUID().toString(),
                timestamp,
                level,
                message.trim()
        );

        logs.add(savedLog);

        return savedLog;
    }


    // =========================================================
    // ADD BULK LOGS
    // =========================================================

    public Map<String, Object> addBulkLogs(
            String bulkText) {

        if (bulkText == null ||
                bulkText.isBlank()) {

            throw new IllegalArgumentException(
                    "Bulk log input is empty"
            );
        }

        String[] lines =
                bulkText.split("\\r?\\n");

        int processed = 0;
        int failed = 0;

        List<String> errors =
                new ArrayList<>();


        for (int i = 0;
             i < lines.length;
             i++) {

            String line =
                    lines[i].trim();

            // Ignore empty lines
            if (line.isEmpty()) {
                continue;
            }

            try {

                LogEntry parsedLog =
                        parseLogLine(line);

                addLog(parsedLog);

                processed++;

            } catch (Exception e) {

                failed++;

                errors.add(
                        "Line " +
                        (i + 1) +
                        ": " +
                        e.getMessage()
                );
            }
        }


        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "totalLines",
                lines.length
        );

        result.put(
                "processed",
                processed
        );

        result.put(
                "failed",
                failed
        );

        result.put(
                "errors",
                errors
        );

        return result;
    }


    // =========================================================
    // PARSE LOG LINE
    // =========================================================

    private LogEntry parseLogLine(
            String line) {

        /*
         Expected format:

         YYYY-MM-DD HH:MM:SS LEVEL MESSAGE

         Example:

         2026-10-04 10:40:01 INFO Application started
        */

        String[] parts =
                line.split("\\s+", 4);

        if (parts.length < 4) {

            throw new IllegalArgumentException(
                    "Invalid format. Expected: " +
                    "YYYY-MM-DD HH:MM:SS LEVEL MESSAGE"
            );
        }

        String timestamp =
                parts[0] + " " + parts[1];

        String level =
                parts[2];

        String message =
                parts[3];

        return new LogEntry(
                null,
                timestamp,
                level,
                message
        );
    }


    // =========================================================
    // GET ALL LOGS
    // =========================================================

    public List<LogEntry> getAllLogs() {

        return new ArrayList<>(logs);
    }


    // =========================================================
    // CLEAR ALL LOGS
    // =========================================================

    public void clearLogs() {

        logs.clear();
    }


    // =========================================================
    // STATISTICS
    // =========================================================

    public Map<String, Object> getStatistics() {

        long info =
                logs.stream()
                        .filter(log ->
                                "INFO".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();

        long warnings =
                logs.stream()
                        .filter(log ->
                                "WARNING".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();

        long errors =
                logs.stream()
                        .filter(log ->
                                "ERROR".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();

        long exceptions =
                logs.stream()
                        .filter(log ->
                                "EXCEPTION".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();


        Map<String, Object> statistics =
                new LinkedHashMap<>();

        statistics.put(
                "totalLogs",
                logs.size()
        );

        statistics.put(
                "info",
                info
        );

        statistics.put(
                "warnings",
                warnings
        );

        statistics.put(
                "errors",
                errors
        );

        statistics.put(
                "exceptions",
                exceptions
        );

        return statistics;
    }


    // =========================================================
    // TOP ERRORS
    // =========================================================

    public List<Map<String, Object>>
    getTopErrors() {

        return logs.stream()

                .filter(log ->
                        "ERROR".equalsIgnoreCase(
                                log.getLevel()
                        )
                        ||
                        "EXCEPTION".equalsIgnoreCase(
                                log.getLevel()
                        )
                )

                .collect(
                        Collectors.groupingBy(
                                LogEntry::getMessage,
                                Collectors.counting()
                        )
                )

                .entrySet()

                .stream()

                .sorted(
                        Map.Entry
                                .<String, Long>
                                comparingByValue()
                                .reversed()
                )

                .limit(10)

                .map(entry -> {

                    Map<String, Object>
                            error =
                            new LinkedHashMap<>();

                    error.put(
                            "message",
                            entry.getKey()
                    );

                    error.put(
                            "count",
                            entry.getValue()
                    );

                    return error;
                })

                .collect(
                        Collectors.toList()
                );
    }


    // =========================================================
    // ALERTS
    // =========================================================

    public List<Map<String, Object>>
    getAlerts() {

        List<Map<String, Object>>
                alerts =
                new ArrayList<>();


        long errors =
                logs.stream()
                        .filter(log ->
                                "ERROR".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();


        long exceptions =
                logs.stream()
                        .filter(log ->
                                "EXCEPTION".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();


        // High error rate
        if (errors >= 3) {

            Map<String, Object>
                    alert =
                    new LinkedHashMap<>();

            alert.put(
                    "type",
                    "HIGH_ERROR_RATE"
            );

            alert.put(
                    "severity",
                    "CRITICAL"
            );

            alert.put(
                    "message",
                    "Critical logs increasing"
            );

            alerts.add(alert);
        }


        // Exception detected
        if (exceptions > 0) {

            Map<String, Object>
                    alert =
                    new LinkedHashMap<>();

            alert.put(
                    "type",
                    "EXCEPTION_DETECTED"
            );

            alert.put(
                    "severity",
                    "CRITICAL"
            );

            alert.put(
                    "message",
                    "Exception detected in system"
            );

            alerts.add(alert);
        }

        return alerts;
    }


    // =========================================================
    // SYSTEM HEALTH
    // =========================================================

    public Map<String, Object>
    getHealth() {

        long errors =
                logs.stream()
                        .filter(log ->
                                "ERROR".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();


        long warnings =
                logs.stream()
                        .filter(log ->
                                "WARNING".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();


        long exceptions =
                logs.stream()
                        .filter(log ->
                                "EXCEPTION".equalsIgnoreCase(
                                        log.getLevel()
                                )
                        )
                        .count();


        /*
         Health calculation:

         Starting score = 100

         WARNING   = -2
         ERROR     = -8
         EXCEPTION = -12
        */

        int score = 100;

        score -=
                warnings * 2;

        score -=
                errors * 8;

        score -=
                exceptions * 12;


        // Keep score between 0 and 100
        score =
                Math.max(
                        0,
                        Math.min(
                                100,
                                score
                        )
                );


        String status;
        String message;


        if (score >= 80) {

            status = "HEALTHY";

            message =
                    "System operating normally";

        } else if (score >= 60) {

            status = "WARNING";

            message =
                    "System requires attention";

        } else {

            status = "CRITICAL";

            message =
                    "Critical issues detected";
        }


        Map<String, Object>
                health =
                new LinkedHashMap<>();

        health.put(
                "score",
                score
        );

        health.put(
                "status",
                status
        );

        health.put(
                "message",
                message
        );

        return health;
    }
}


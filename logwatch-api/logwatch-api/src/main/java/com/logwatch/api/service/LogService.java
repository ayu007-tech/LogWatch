
package com.logwatch.api.service;

import com.logwatch.api.model.LogEntry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final List<LogEntry> logs = new CopyOnWriteArrayList<>();
    private final LogStreamService streamService;

    public LogService(LogStreamService streamService) {
        this.streamService = streamService;
    }

    public LogEntry addLog(LogEntry log) {
        if (log == null) {
            throw new IllegalArgumentException("Log cannot be null");
        }

        if (log.getLevel() == null || log.getLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("Log level is required");
        }

        if (log.getMessage() == null || log.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Log message is required");
        }

        String level = log.getLevel().trim().toUpperCase();

        if (!isValidLevel(level)) {
            throw new IllegalArgumentException(
                    "Invalid log level. Use INFO, WARNING, ERROR or EXCEPTION");
        }

        log.setId(UUID.randomUUID().toString());
        log.setLevel(level);
        log.setMessage(log.getMessage().trim());

        if (log.getTimestamp() == null || log.getTimestamp().trim().isEmpty()) {
            log.setTimestamp(Instant.now().toString());
        }

        logs.add(log);
        streamService.publish(log);

        return log;
    }

    public Map<String, Object> addBulkLogs(String bulkText) {
        if (bulkText == null || bulkText.trim().isEmpty()) {
            throw new IllegalArgumentException("Bulk log input cannot be empty");
        }

        String[] lines = bulkText.split("\\r?\\n");
        int processed = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            try {
                addLog(parseLogLine(line));
                processed++;
            } catch (Exception e) {
                failed++;
                errors.add("Line " + (i + 1) + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLines", lines.length);
        result.put("processed", processed);
        result.put("failed", failed);
        result.put("errors", errors);
        return result;
    }

    private LogEntry parseLogLine(String line) {
        if (line.length() < 22) {
            throw new IllegalArgumentException("Invalid log format");
        }

        String timestamp = line.substring(0, 19);
        String remaining = line.substring(19).trim();
        int firstSpace = remaining.indexOf(" ");

        if (firstSpace == -1) {
            throw new IllegalArgumentException("Missing log level or message");
        }

        String level = remaining.substring(0, firstSpace).trim();
        String message = remaining.substring(firstSpace + 1).trim();

        if (message.isEmpty()) {
            throw new IllegalArgumentException("Log message is missing");
        }

        return new LogEntry(null, timestamp, level, message);
    }

    private boolean isValidLevel(String level) {
        return level.equals("INFO")
                || level.equals("WARNING")
                || level.equals("ERROR")
                || level.equals("EXCEPTION");
    }

    private long countLevel(List<LogEntry> source, String level) {
        return source.stream()
                .filter(log -> level.equals(log.getLevel()))
                .count();
    }

    public List<LogEntry> getAllLogs() {
        return logs.stream()
                .sorted(Comparator.comparing(
                        LogEntry::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics() {
        List<LogEntry> snapshot = new ArrayList<>(logs);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalLogs", snapshot.size());
        stats.put("info", countLevel(snapshot, "INFO"));
        stats.put("warnings", countLevel(snapshot, "WARNING"));
        stats.put("errors", countLevel(snapshot, "ERROR"));
        stats.put("exceptions", countLevel(snapshot, "EXCEPTION"));
        return stats;
    }

    public List<Map<String, Object>> getTopErrors() {
        Map<String, Long> grouped = logs.stream()
                .filter(log -> "ERROR".equals(log.getLevel())
                        || "EXCEPTION".equals(log.getLevel()))
                .collect(Collectors.groupingBy(
                        LogEntry::getMessage, Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("message", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        long errors = countLevel(logs, "ERROR");
        long exceptions = countLevel(logs, "EXCEPTION");

        if (errors >= 3) {
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("type", "HIGH_ERROR_RATE");
            alert.put("severity", "CRITICAL");
            alert.put("message", "High number of errors detected");
            alerts.add(alert);
        }

        if (exceptions > 0) {
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("type", "EXCEPTION_DETECTED");
            alert.put("severity", "CRITICAL");
            alert.put("message", "Exception detected in logs");
            alerts.add(alert);
        }

        return alerts;
    }

    public Map<String, Object> getHealth() {
        long warnings = countLevel(logs, "WARNING");
        long errors = countLevel(logs, "ERROR");
        long exceptions = countLevel(logs, "EXCEPTION");

        int score = (int) Math.max(
                0, 100 - errors * 8 - warnings * 2 - exceptions * 12);

        String status = score >= 80 ? "HEALTHY"
                : score >= 60 ? "WARNING" : "CRITICAL";

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("score", score);
        health.put("status", status);
        return health;
    }

    public void deleteAllLogs() {
        logs.clear();
    }
}

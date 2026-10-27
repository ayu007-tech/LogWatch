```java
package com.logwatch.api.controller;

import com.logwatch.api.model.LogEntry;
import com.logwatch.api.service.LogService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5500")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    // Test API
    @GetMapping("/test")
    public String test() {
        return "LogWatch API is running!";
    }

    // Add single log
    @PostMapping("/logs")
    public ResponseEntity<?> addLog(@RequestBody LogEntry log) {

        try {
            LogEntry savedLog = logService.addLog(log);

            return ResponseEntity.ok(savedLog);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Add multiple logs
    @PostMapping("/logs/bulk")
    public ResponseEntity<?> addBulkLogs(
            @RequestBody Map<String, String> request) {

        try {

            String logs = request.get("logs");

            Map<String, Object> result =
                    logService.addBulkLogs(logs);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get all logs
    @GetMapping("/logs")
    public List<LogEntry> getLogs() {
        return logService.getAllLogs();
    }

    // Clear all logs
    @DeleteMapping("/logs")
    public ResponseEntity<?> clearLogs() {

        logService.clearLogs();

        return ResponseEntity.ok(
                Map.of(
                        "message", "All logs cleared successfully",
                        "totalLogs", 0
                )
        );
    }

    // Statistics
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return logService.getStatistics();
    }

    // Top errors
    @GetMapping("/errors")
    public List<Map<String, Object>> getErrors() {
        return logService.getTopErrors();
    }

    // Alerts
    @GetMapping("/alerts")
    public List<Map<String, Object>> getAlerts() {
        return logService.getAlerts();
    }

    // System health
    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        return logService.getHealth();
    }
}


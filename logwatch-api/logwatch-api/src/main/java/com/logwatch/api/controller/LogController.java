
package com.logwatch.api.controller;

import com.logwatch.api.model.LogEntry;
import com.logwatch.api.service.LogService;
import com.logwatch.api.service.LogStreamService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5500")
public class LogController {

    private final LogService logService;
    private final LogStreamService streamService;

    public LogController(
            LogService logService,
            LogStreamService streamService) {
        this.logService = logService;
        this.streamService = streamService;
    }

    @GetMapping("/test")
    public String test() {
        return "LogWatch API is running!";
    }

    @GetMapping(value = "/logs/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        return streamService.subscribe();
    }

    @PostMapping("/logs")
    public ResponseEntity<?> addLog(@RequestBody LogEntry log) {
        try {
            return ResponseEntity.ok(logService.addLog(log));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logs/bulk")
    public ResponseEntity<?> addBulkLogs(
            @RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(
                    logService.addBulkLogs(request.get("logs")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/logs")
    public List<LogEntry> getLogs() {
        return logService.getAllLogs();
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return logService.getStatistics();
    }

    @GetMapping("/errors")
    public List<Map<String, Object>> getErrors() {
        return logService.getTopErrors();
    }

    @GetMapping("/alerts")
    public List<Map<String, Object>> getAlerts() {
        return logService.getAlerts();
    }

    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        return logService.getHealth();
    }

    @DeleteMapping("/logs")
    public ResponseEntity<Void> deleteAllLogs() {
        logService.deleteAllLogs();
        return ResponseEntity.noContent().build();
    }
}


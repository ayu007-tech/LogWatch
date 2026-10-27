package com.logwatch.api.model;

public class LogEntry {

    private String id;
    private String timestamp;
    private String level;
    private String message;

    // Default constructor
    public LogEntry() {
    }

    // Parameterized constructor
    public LogEntry(String id, String timestamp, String level, String message) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
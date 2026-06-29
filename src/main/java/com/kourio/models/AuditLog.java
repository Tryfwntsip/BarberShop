package com.kourio.models;

import java.time.LocalDateTime;

public class AuditLog {
    private int id;
    private String username;
    private String action;
    private String ipAddress;
    private LocalDateTime timestamp;

    // Constructors
    public AuditLog() {
        this.ipAddress = "127.0.0.1";
    }

    public AuditLog(int id, String username, String action, String ipAddress, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] User: " + username + " - Action: " + action;
    }
}

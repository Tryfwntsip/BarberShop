package com.kourio.daos;

import com.kourio.models.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public boolean logEvent(String username, String action, String ipAddress) throws SQLException {
        String sql = "INSERT INTO audit_log (username, action, ip_address) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, action);
            ps.setString(3, ipAddress != null ? ipAddress : "127.0.0.1");
            return ps.executeUpdate() > 0;
        }
    }

    public List<AuditLog> getLogs(int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getInt("id"));
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setIpAddress(rs.getString("ip_address"));
                    
                    Timestamp ts = rs.getTimestamp("timestamp");
                    if (ts != null) {
                        log.setTimestamp(ts.toLocalDateTime());
                    }
                    list.add(log);
                }
            }
        }
        return list;
    }
}

package com.kourio.daos;

import com.kourio.models.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public Service getServiceById(int id) throws SQLException {
        String sql = "SELECT * FROM services WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractServiceFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Service> getAllServices(boolean includeInactive) throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = includeInactive ? "SELECT * FROM services" : "SELECT * FROM services WHERE is_active = true";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                services.add(extractServiceFromResultSet(rs));
            }
        }
        return services;
    }

    public boolean createService(Service service) throws SQLException {
        String sql = "INSERT INTO services (name, price, duration_minutes, category, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, service.getName());
            ps.setBigDecimal(2, service.getPrice());
            ps.setInt(3, service.getDurationMinutes());
            ps.setString(4, service.getCategory());
            ps.setBoolean(5, service.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        service.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateService(Service service) throws SQLException {
        String sql = "UPDATE services SET name = ?, price = ?, duration_minutes = ?, category = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service.getName());
            ps.setBigDecimal(2, service.getPrice());
            ps.setInt(3, service.getDurationMinutes());
            ps.setString(4, service.getCategory());
            ps.setBoolean(5, service.isActive());
            ps.setInt(6, service.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deactivateService(int serviceId) throws SQLException {
        String sql = "UPDATE services SET is_active = false WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hardDeleteService(int serviceId) throws SQLException {
        // First delete related appointments
        String deleteAppointmentsSql = "DELETE FROM appointments WHERE service_id = ?";
        String deleteServiceSql = "DELETE FROM services WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteAppointmentsSql)) {
                ps1.setInt(1, serviceId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(deleteServiceSql)) {
                ps2.setInt(1, serviceId);
                int affected = ps2.executeUpdate();
                conn.commit();
                return affected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public boolean hasActiveFutureAppointments(int serviceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE service_id = ? " +
                     "AND appointment_date >= CURDATE() " +
                     "AND status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Service extractServiceFromResultSet(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setId(rs.getInt("id"));
        service.setName(rs.getString("name"));
        service.setPrice(rs.getBigDecimal("price"));
        service.setDurationMinutes(rs.getInt("duration_minutes"));
        service.setCategory(rs.getString("category"));
        service.setActive(rs.getBoolean("is_active"));
        
        Timestamp createTS = rs.getTimestamp("created_at");
        if (createTS != null) {
            service.setCreatedAt(createTS.toLocalDateTime());
        }
        return service;
    }
}

package com.kourio.daos;

import com.kourio.models.Role;
import com.kourio.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<User> getAllBarbers() throws SQLException {
        List<User> barbers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'BARBER'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                barbers.add(extractUserFromResultSet(rs));
            }
        }
        return barbers;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        }
        return users;
    }

    public boolean createUser(User user, String plaintextPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, first_name, last_name, phone, email, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        String hashedPassword = BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getLastName());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getEmail());
            ps.setBoolean(8, user.isActive());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, phone = ?, email = ?, is_active = ?, " +
                     "failed_logins = ?, locked_until = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getEmail());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getFailedLogins());
            
            if (user.getLockedUntil() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(user.getLockedUntil()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            
            ps.setInt(8, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean resetPassword(int userId, String newPlaintextPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, failed_logins = 0, locked_until = NULL WHERE id = ?";
        String hashedPassword = BCrypt.hashpw(newPlaintextPassword, BCrypt.gensalt());
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean createBarberDetails(int barberId, LocalTime start, LocalTime end, String specialties) throws SQLException {
        String sql = "INSERT INTO barber_details (barber_id, working_hours_start, working_hours_end, specialties) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, barberId);
            ps.setTime(2, Time.valueOf(start));
            ps.setTime(3, Time.valueOf(end));
            ps.setString(4, specialties);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateBarberDetails(int barberId, LocalTime start, LocalTime end, String specialties) throws SQLException {
        String sql = "UPDATE barber_details SET working_hours_start = ?, working_hours_end = ?, specialties = ? WHERE barber_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(start));
            ps.setTime(2, Time.valueOf(end));
            ps.setString(3, specialties);
            ps.setInt(4, barberId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hardDeleteBarber(int barberId) throws SQLException {
        // Delete in order: appointments -> barber_details -> users
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM appointments WHERE barber_id = ?")) {
                    ps.setInt(1, barberId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM barber_details WHERE barber_id = ?")) {
                    ps.setInt(1, barberId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ? AND role = 'BARBER'")) {
                    ps.setInt(1, barberId);
                    int affected = ps.executeUpdate();
                    conn.commit();
                    return affected > 0;
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public String getBarberSpecialties(int barberId) throws SQLException {
        String sql = "SELECT specialties FROM barber_details WHERE barber_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, barberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("specialties");
                  }
              }
          }
          return "";
    }

    /** Working-hours window for a barber. */
    public record WorkingHours(LocalTime start, LocalTime end) {}

    /**
     * Returns the barber's configured working hours, or {@code null} if no
     * barber_details row exists (callers should fall back to their own defaults).
     */
    public WorkingHours getBarberWorkingHours(int barberId) throws SQLException {
        String sql = "SELECT working_hours_start, working_hours_end FROM barber_details WHERE barber_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, barberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new WorkingHours(
                        rs.getTime("working_hours_start").toLocalTime(),
                        rs.getTime("working_hours_end").toLocalTime());
                }
            }
        }
        return null;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));
        user.setFailedLogins(rs.getInt("failed_logins"));
        
        Timestamp lockTS = rs.getTimestamp("locked_until");
        if (lockTS != null) {
            user.setLockedUntil(lockTS.toLocalDateTime());
        }
        
        Timestamp createTS = rs.getTimestamp("created_at");
        if (createTS != null) {
            user.setCreatedAt(createTS.toLocalDateTime());
        }
        return user;
    }
}

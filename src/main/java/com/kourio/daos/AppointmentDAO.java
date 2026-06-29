package com.kourio.daos;

import com.kourio.models.Appointment;
import com.kourio.models.AppointmentStatus;
import com.kourio.models.Service;
import com.kourio.models.User;
import com.kourio.models.Role;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentDAO {

    public Appointment getAppointmentById(int id) throws SQLException {
        String sql = "SELECT a.*, " +
                     "c.username as c_username, c.first_name as c_fname, c.last_name as c_lname, c.phone as c_phone, c.email as c_email, " +
                     "b.username as b_username, b.first_name as b_fname, b.last_name as b_lname, b.phone as b_phone, b.email as b_email, " +
                     "s.name as s_name, s.category as s_cat " +
                     "FROM appointments a " +
                     "JOIN users c ON a.customer_id = c.id " +
                     "JOIN users b ON a.barber_id = b.id " +
                     "JOIN services s ON a.service_id = s.id " +
                     "WHERE a.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractAppointmentFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public Appointment getCustomerAppointmentById(int appointmentId, int customerId) throws SQLException {
        String sql = "SELECT a.*, " +
                     "c.username as c_username, c.first_name as c_fname, c.last_name as c_lname, c.phone as c_phone, c.email as c_email, " +
                     "b.username as b_username, b.first_name as b_fname, b.last_name as b_lname, b.phone as b_phone, b.email as b_email, " +
                     "s.name as s_name, s.category as s_cat " +
                     "FROM appointments a " +
                     "JOIN users c ON a.customer_id = c.id " +
                     "JOIN users b ON a.barber_id = b.id " +
                     "JOIN services s ON a.service_id = s.id " +
                     "WHERE a.id = ? AND a.customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractAppointmentFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Appointment> getCustomerAppointments(int customerId) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "c.username as c_username, c.first_name as c_fname, c.last_name as c_lname, c.phone as c_phone, c.email as c_email, " +
                     "b.username as b_username, b.first_name as b_fname, b.last_name as b_lname, b.phone as b_phone, b.email as b_email, " +
                     "s.name as s_name, s.category as s_cat " +
                     "FROM appointments a " +
                     "JOIN users c ON a.customer_id = c.id " +
                     "JOIN users b ON a.barber_id = b.id " +
                     "JOIN services s ON a.service_id = s.id " +
                     "WHERE a.customer_id = ? " +
                     "ORDER BY a.appointment_date DESC, a.start_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppointmentFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Appointment> getBarberAppointments(int barberId, LocalDate date) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "c.username as c_username, c.first_name as c_fname, c.last_name as c_lname, c.phone as c_phone, c.email as c_email, " +
                     "b.username as b_username, b.first_name as b_fname, b.last_name as b_lname, b.phone as b_phone, b.email as b_email, " +
                     "s.name as s_name, s.category as s_cat " +
                     "FROM appointments a " +
                     "JOIN users c ON a.customer_id = c.id " +
                     "JOIN users b ON a.barber_id = b.id " +
                     "JOIN services s ON a.service_id = s.id " +
                     "WHERE a.barber_id = ? AND a.appointment_date = ? " +
                     "ORDER BY a.start_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, barberId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppointmentFromResultSet(rs));
                }
            }
        }
        return list;
    }

    public List<Appointment> searchCustomerHistory(int barberId, String query) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "c.username as c_username, c.first_name as c_fname, c.last_name as c_lname, c.phone as c_phone, c.email as c_email, " +
                     "b.username as b_username, b.first_name as b_fname, b.last_name as b_lname, b.phone as b_phone, b.email as b_email, " +
                     "s.name as s_name, s.category as s_cat " +
                     "FROM appointments a " +
                     "JOIN users c ON a.customer_id = c.id " +
                     "JOIN users b ON a.barber_id = b.id " +
                     "JOIN services s ON a.service_id = s.id " +
                     "WHERE a.barber_id = ? AND (c.first_name LIKE ? OR c.last_name LIKE ? OR c.phone LIKE ?) " +
                     "ORDER BY a.appointment_date DESC, a.start_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + query + "%";
            ps.setInt(1, barberId);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            ps.setString(4, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppointmentFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if a specific slot is available for a barber on a given date.
     * Checks:
     * 1. If slot falls within the barber's working hours.
     * 2. If slot overlaps with any active (non-cancelled) bookings.
     */
    public boolean isSlotAvailable(int barberId, LocalDate date, LocalTime startTime, int durationMinutes) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return isSlotAvailable(conn, barberId, date, startTime, durationMinutes);
        }
    }

    /**
     * Connection-bound availability check. Runs on the supplied connection so it can
     * participate in an outer transaction (e.g. the SELECT ... FOR UPDATE lock taken by
     * {@link #bookAppointment}). The connection is NOT closed here.
     */
    private boolean isSlotAvailable(Connection conn, int barberId, LocalDate date, LocalTime startTime, int durationMinutes) throws SQLException {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        // 1. Fetch barber working hours
        String barberHoursSql = "SELECT working_hours_start, working_hours_end FROM barber_details WHERE barber_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(barberHoursSql)) {
            ps.setInt(1, barberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalTime workStart = rs.getTime("working_hours_start").toLocalTime();
                    LocalTime workEnd = rs.getTime("working_hours_end").toLocalTime();
                    if (startTime.isBefore(workStart) || endTime.isAfter(workEnd)) {
                        return false; // Out of working hours
                    }
                } else {
                    return false; // Barber details not found
                }
            }
        }

        // 2. Check overlap with other active appointments
        String overlapSql = "SELECT start_time, end_time FROM appointments " +
                             "WHERE barber_id = ? AND appointment_date = ? " +
                             "AND status != 'CANCELLED'";
        try (PreparedStatement ps = conn.prepareStatement(overlapSql)) {
            ps.setInt(1, barberId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalTime bookedStart = rs.getTime("start_time").toLocalTime();
                    LocalTime bookedEnd = rs.getTime("end_time").toLocalTime();

                    // Overlap check: start1 < end2 AND start2 < end1
                    if (startTime.isBefore(bookedEnd) && bookedStart.isBefore(endTime)) {
                        return false; // Overlap detected
                    }
                }
            }
        }
        return true;
    }

    /**
     * Creates an appointment using an ACID transaction.
     * Double checks availability right before insertion to prevent race conditions.
     */
    public boolean bookAppointment(Appointment app) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start ACID transaction
            
            // Check availability inside transaction using SELECT FOR UPDATE on appointments
            // (this locks the rows for the barber and date to avoid concurrent bookings)
            String lockSql = "SELECT id FROM appointments WHERE barber_id = ? AND appointment_date = ? " +
                             "AND status != 'CANCELLED' FOR UPDATE";
            try (PreparedStatement lockPs = conn.prepareStatement(lockSql)) {
                lockPs.setInt(1, app.getBarberId());
                lockPs.setDate(2, Date.valueOf(app.getAppointmentDate()));
                lockPs.executeQuery(); // Locks active appointments for barber on date
            }

            // Check if slot is available within this transaction (so the FOR UPDATE lock applies)
            boolean available = isSlotAvailable(conn, app.getBarberId(), app.getAppointmentDate(), app.getStartTime(), app.getDurationAtBooking());
            if (!available) {
                conn.rollback();
                return false;
            }

            // Insert new booking
            String insertSql = "INSERT INTO appointments (customer_id, barber_id, service_id, appointment_date, " +
                               "start_time, end_time, price_at_booking, duration_at_booking, status, notes) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, app.getCustomerId());
                ps.setInt(2, app.getBarberId());
                ps.setInt(3, app.getServiceId());
                ps.setDate(4, Date.valueOf(app.getAppointmentDate()));
                ps.setTime(5, Time.valueOf(app.getStartTime()));
                ps.setTime(6, Time.valueOf(app.getEndTime()));
                ps.setBigDecimal(7, app.getPriceAtBooking());
                ps.setInt(8, app.getDurationAtBooking());
                ps.setString(9, app.getStatus().name());
                ps.setString(10, app.getNotes());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            app.setId(generatedKeys.getInt(1));
                        }
                    }
                    conn.commit(); // Commit transaction
                    return true;
                }
            }
            conn.rollback();
            return false;
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Ignore or log
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public boolean updateAppointmentStatus(int appointmentId, AppointmentStatus status, String notes) throws SQLException {
        String sql = "UPDATE appointments SET status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, notes);
            ps.setInt(3, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelCustomerAppointment(int appointmentId, int customerId, String notes) throws SQLException {
        String sql = "UPDATE appointments SET status = 'CANCELLED', notes = ? " +
                     "WHERE id = ? AND customer_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notes);
            ps.setInt(2, appointmentId);
            ps.setInt(3, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hasBarberFutureAppointments(int barberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE barber_id = ? " +
                     "AND appointment_date >= CURDATE() " +
                     "AND status NOT IN ('CANCELLED', 'COMPLETED', 'NO_SHOW')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, barberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * UC-10 Statistics calculation
     */
    public Map<String, Object> getStatistics(LocalDate start, LocalDate end, Integer barberId, Integer serviceId) throws SQLException {
        Map<String, Object> stats = emptyStatistics();
        
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "COUNT(*) as total_bookings, " +
            "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completed_count, " +
            "COALESCE(SUM(CASE WHEN status = 'NO_SHOW' THEN 1 ELSE 0 END), 0) as noshow_count, " +
            "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN price_at_booking ELSE 0 END), 0) as total_revenue " +
            "FROM appointments " +
            "WHERE appointment_date BETWEEN ? AND ? " +
            "AND status != 'CANCELLED'"
        );
        
        if (barberId != null) sql.append(" AND barber_id = ?");
        if (serviceId != null) sql.append(" AND service_id = ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            int paramIndex = 3;
            if (barberId != null) ps.setInt(paramIndex++, barberId);
            if (serviceId != null) ps.setInt(paramIndex++, serviceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_bookings");
                    int completed = rs.getInt("completed_count");
                    int noshow = rs.getInt("noshow_count");
                    BigDecimal revenue = rs.getBigDecimal("total_revenue");
                    if (revenue == null) revenue = BigDecimal.ZERO;

                    stats.put("totalBookings", total);
                    stats.put("completedBookings", completed);
                    stats.put("noShowBookings", noshow);
                    stats.put("totalRevenue", revenue);

                    // Average revenue = revenue / completed
                    BigDecimal avgRevenue = completed > 0 
                        ? revenue.divide(BigDecimal.valueOf(completed), 2, java.math.RoundingMode.HALF_UP) 
                        : BigDecimal.ZERO;
                    stats.put("avgRevenue", avgRevenue);

                    // No-show rate = (noShow / total) * 100
                    double noShowRate = total > 0 ? ((double) noshow / total) * 100 : 0.0;
                    stats.put("noShowRate", noShowRate);
                }
            }
        }
        return stats;
    }

    private Map<String, Object> emptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings", 0);
        stats.put("completedBookings", 0);
        stats.put("noShowBookings", 0);
        stats.put("totalRevenue", BigDecimal.ZERO);
        stats.put("avgRevenue", BigDecimal.ZERO);
        stats.put("noShowRate", 0.0);
        return stats;
    }

    public List<Appointment> getBarberAppointmentsByMonth(int barberId, int year, int month) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, " +
                     "c.username as c_username, c.first_name as c_fname, c.last_name as c_lname, c.phone as c_phone, c.email as c_email, " +
                     "b.username as b_username, b.first_name as b_fname, b.last_name as b_lname, b.phone as b_phone, b.email as b_email, " +
                     "s.name as s_name, s.category as s_cat " +
                     "FROM appointments a " +
                     "JOIN users c ON a.customer_id = c.id " +
                     "JOIN users b ON a.barber_id = b.id " +
                     "JOIN services s ON a.service_id = s.id " +
                     "WHERE a.barber_id = ? AND YEAR(a.appointment_date) = ? AND MONTH(a.appointment_date) = ? " +
                     "ORDER BY a.appointment_date ASC, a.start_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, barberId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppointmentFromResultSet(rs));
                }
            }
        }
        return list;
    }

    private Appointment extractAppointmentFromResultSet(ResultSet rs) throws SQLException {
        Appointment app = new Appointment();
        app.setId(rs.getInt("id"));
        app.setCustomerId(rs.getInt("customer_id"));
        app.setBarberId(rs.getInt("barber_id"));
        app.setServiceId(rs.getInt("service_id"));
        app.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        app.setStartTime(rs.getTime("start_time").toLocalTime());
        app.setEndTime(rs.getTime("end_time").toLocalTime());
        app.setPriceAtBooking(rs.getBigDecimal("price_at_booking"));
        app.setDurationAtBooking(rs.getInt("duration_at_booking"));
        app.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        app.setNotes(rs.getString("notes"));
        
        Timestamp statusTS = rs.getTimestamp("status_updated_at");
        if (statusTS != null) {
            app.setStatusUpdatedAt(statusTS.toLocalDateTime());
        }
        Timestamp createTS = rs.getTimestamp("created_at");
        if (createTS != null) {
            app.setCreatedAt(createTS.toLocalDateTime());
        }

        // Populate Associated Customer
        User customer = new User();
        customer.setId(app.getCustomerId());
        customer.setUsername(rs.getString("c_username"));
        customer.setFirstName(rs.getString("c_fname"));
        customer.setLastName(rs.getString("c_lname"));
        customer.setPhone(rs.getString("c_phone"));
        customer.setEmail(rs.getString("c_email"));
        customer.setRole(Role.CUSTOMER);
        app.setCustomer(customer);

        // Populate Associated Barber
        User barber = new User();
        barber.setId(app.getBarberId());
        barber.setUsername(rs.getString("b_username"));
        barber.setFirstName(rs.getString("b_fname"));
        barber.setLastName(rs.getString("b_lname"));
        barber.setPhone(rs.getString("b_phone"));
        barber.setEmail(rs.getString("b_email"));
        barber.setRole(Role.BARBER);
        app.setBarber(barber);

        // Populate Associated Service
        Service service = new Service();
        service.setId(app.getServiceId());
        service.setName(rs.getString("s_name"));
        service.setCategory(rs.getString("s_cat"));
        service.setPrice(app.getPriceAtBooking());
        service.setDurationMinutes(app.getDurationAtBooking());
        app.setService(service);

        return app;
    }
}

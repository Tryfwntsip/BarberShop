-- SQL Schema for Kourio Appointment Management System
-- Database: kourio_db
-- Target: local MySQL instance (version 8.0+)

CREATE DATABASE IF NOT EXISTS kourio_db;
USE kourio_db;

-- 1. Disable foreign key checks to drop tables in correct order if they exist
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS barber_details;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. Create 'users' table
-- Holds login credentials, details, and role for Customers, Barbers, and the Owner.
-- Includes failed_logins and locked_until for the 5-failed-attempts lockout policy.
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Stored as BCrypt hash
    role ENUM('CUSTOMER', 'BARBER', 'OWNER') NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    failed_logins INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create 'barber_details' table
-- Holds barber-specific settings: working hours and specialties.
CREATE TABLE barber_details (
    barber_id INT PRIMARY KEY,
    working_hours_start TIME NOT NULL DEFAULT '09:00:00',
    working_hours_end TIME NOT NULL DEFAULT '17:00:00',
    specialties VARCHAR(255),
    FOREIGN KEY (barber_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create 'services' table
-- Stores services provided by the barber shop, including pricing and durations.
CREATE TABLE services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INT NOT NULL, -- E.g. 15, 30, 45, 60 (multiples of 15)
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE, -- For soft deletes (UC-09)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Create 'appointments' table
-- Stores appointments booked by Customers with Barbers for specific Services.
-- Price and duration are stored at booking time to ensure historical pricing stability (§3.3.5).
-- Unique index is set on (barber_id, appointment_date, start_time, is_active_booking) to prevent double-bookings.
-- By using a generated column 'is_active_booking' that evaluates to NULL for cancelled appointments,
-- MySQL unique indexing allows multiple cancelled slots but only one active appointment.
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    barber_id INT NOT NULL,
    service_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price_at_booking DECIMAL(10, 2) NOT NULL,
    duration_at_booking INT NOT NULL,
    status ENUM('CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'NO_SHOW', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    notes TEXT, -- Barber comments/notes (UC-07)
    status_updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Stored generated column for double-booking prevention constraint
    is_active_booking TINYINT GENERATED ALWAYS AS (
        CASE WHEN status = 'CANCELLED' THEN NULL ELSE 1 END
    ) STORED,
    
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (barber_id) REFERENCES users(id),
    FOREIGN KEY (service_id) REFERENCES services(id),
    
    -- Prevent overlapping/duplicate active bookings for the same barber at the same date and start time
    UNIQUE KEY unique_barber_slot (barber_id, appointment_date, start_time, is_active_booking)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Create 'audit_log' table
-- Stores logs of logins, logouts, lockouts, and cancellations for security auditing purposes (§3.3.3 & UC-05).
CREATE TABLE audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL, -- e.g. 'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'ACCOUNT_LOCKOUT', 'APPOINTMENT_CANCELLED'
    ip_address VARCHAR(45) DEFAULT '127.0.0.1',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==========================================
-- SEED DATA (For local development & testing)
-- ==========================================

-- All passwords are encrypted with BCrypt (plaintext is 'password123')
-- BCrypt Hash: $2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K

-- Add Owner (Admin)
INSERT INTO users (username, password_hash, role, first_name, last_name, phone, email, is_active)
VALUES ('admin', '$2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K', 'OWNER', 'Απόλλων', 'Τρύφων', '2101234567', 'admin@kourio.gr', 1);

-- Add Barbers
INSERT INTO users (id, username, password_hash, role, first_name, last_name, phone, email, is_active)
VALUES 
(2, 'barber1', '$2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K', 'BARBER', 'Νίκος', 'Γεωργίου', '6971111111', 'nikos@kourio.gr', 1),
(3, 'barber2', '$2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K', 'BARBER', 'Κώστας', 'Δημητρίου', '6972222222', 'kostas@kourio.gr', 1);

-- Add Barber details
INSERT INTO barber_details (barber_id, working_hours_start, working_hours_end, specialties)
VALUES 
(2, '09:00:00', '17:00:00', 'Κλασικό κούρεμα, Ξύρισμα με φαλτσέτα'),
(3, '10:00:00', '18:00:00', 'Μοντέρνα κουρέματα, Περιποίηση γενειάδας');

-- Add Customers
INSERT INTO users (username, password_hash, role, first_name, last_name, phone, email, is_active)
VALUES 
('customer1', '$2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K', 'CUSTOMER', 'Δημήτρης', 'Βασιλείου', '6973333333', 'dimitris@gmail.com', 1),
('customer2', '$2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K', 'CUSTOMER', 'Αλέξης', 'Νικολάου', '6974444444', 'alexis@yahoo.com', 1);

-- Add Services
INSERT INTO services (name, price, duration_minutes, category, is_active)
VALUES 
('Κούρεμα (Ανδρικό)', 15.00, 30, 'Μαλλιά', 1),
('Ξύρισμα (Παραδοσιακό)', 10.00, 15, 'Γένια', 1),
('Κούρεμα & Ξύρισμα (Combo)', 22.00, 45, 'Combo', 1),
('Περιποίηση Γενειάδας', 8.00, 15, 'Γένια', 1);

-- Add 2 additional Barbers
INSERT INTO users (id, username, password_hash, role, first_name, last_name, phone, email, is_active)
VALUES 
(6, 'barber3', '$2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K', 'BARBER', 'Σταύρος', 'Αντωνίου', '6975555555', 'stavros@kourio.gr', 1),
(7, 'barber4', '$2a$10$GHRUokCK3LQZQM9l.F7tVeGee2k7EJAbz8SZYrLl/m465gCVoMN/K', 'BARBER', 'Μάριος', 'Κωνσταντίνου', '6976666666', 'marios@kourio.gr', 1);

INSERT INTO barber_details (barber_id, working_hours_start, working_hours_end, specialties)
VALUES 
(6, '08:30:00', '16:30:00', 'Fade κουρέματα, Σχέδια στα μαλλιά'),
(7, '11:00:00', '19:00:00', 'Χρώμα μαλλιών, Θεραπείες κερατίνης');

-- Add 10 random demo appointments
INSERT INTO appointments (customer_id, barber_id, service_id, appointment_date, start_time, end_time, price_at_booking, duration_at_booking, status, notes)
VALUES 
(4, 2, 1, '2026-05-22', '09:00:00', '09:30:00', 15.00, 30, 'CONFIRMED', 'Κλασικό κούρεμα'),
(5, 3, 2, '2026-05-22', '10:00:00', '10:15:00', 10.00, 15, 'CONFIRMED', ''),
(4, 2, 3, '2026-05-23', '11:00:00', '11:45:00', 22.00, 45, 'CONFIRMED', 'Combo πακέτο'),
(5, 6, 1, '2026-05-23', '09:00:00', '09:30:00', 15.00, 30, 'CONFIRMED', 'Fade κούρεμα'),
(4, 7, 4, '2026-05-24', '12:00:00', '12:15:00', 8.00, 15, 'CONFIRMED', 'Περιποίηση γενειάδας'),
(5, 2, 1, '2026-05-19', '09:30:00', '10:00:00', 15.00, 30, 'COMPLETED', 'Ολοκληρώθηκε κανονικά'),
(4, 3, 3, '2026-05-18', '14:00:00', '14:45:00', 22.00, 45, 'COMPLETED', 'Πολύ ευχαριστημένος'),
(5, 2, 2, '2026-05-17', '10:00:00', '10:15:00', 10.00, 15, 'NO_SHOW', 'Δεν εμφανίστηκε'),
(4, 6, 1, '2026-05-20', '08:30:00', '09:00:00', 15.00, 30, 'CANCELLED', 'Ακυρώθηκε από τον πελάτη'),
(5, 7, 4, '2026-05-25', '15:00:00', '15:15:00', 8.00, 15, 'CONFIRMED', 'Πρώτη φορά πελάτης');

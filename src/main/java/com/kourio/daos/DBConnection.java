package com.kourio.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/kourio_db?useSSL=false&serverTimezone=Europe/Athens&allowPublicKeyRetrieval=true";

    // Credentials are resolved from (in order): system property -> environment variable -> local default.
    // This keeps a working out-of-the-box local setup while allowing overrides without editing source.
    private static String url = resolve("kourio.db.url", "KOURIO_DB_URL", DEFAULT_URL);
    private static String username = resolve("kourio.db.user", "KOURIO_DB_USER", "root");
    // No real credentials in source. Set the password via the KOURIO_DB_PASSWORD env var
    // (or the kourio.db.password system property). The literal below is only a placeholder.
    private static String password = resolve("kourio.db.password", "KOURIO_DB_PASSWORD", "changeme");

    private DBConnection() {
        // Prevent instantiation
    }

    private static String resolve(String sysProp, String envVar, String fallback) {
        String value = System.getProperty(sysProp);
        if (value == null || value.isBlank()) {
            value = System.getenv(envVar);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public static void configure(String dbUrl, String dbUser, String dbPass) {
        url = dbUrl;
        username = dbUser;
        password = dbPass;
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Load driver explicitly (useful for desktop application compatibility)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Connector/J driver not found", e);
        }
        return DriverManager.getConnection(url, username, password);
    }
}

package com.example.jdbc_assignment.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/jdbc_connection?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "admin";
    private static final String PASSWORD = "@El_bosro123";


    public static Connection getConnection() throws SQLException {
        try {
            System.out.println("Attempting to connect to database...");
            System.out.println("URL: " + URL);
            System.out.println("User: " + USER);

            // Test if driver is available
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("JDBC Driver found!");

            // Try to connect with properties
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("allowPublicKeyRetrieval", "true");

            Connection conn = DriverManager.getConnection(URL, props);
            System.out.println("Successfully connected to database!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found!");
            throw new SQLException("JDBC Driver not found.", e);
        } catch (SQLException e) {
            System.out.println("Failed to connect to database!");
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
            throw e;
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            // Try a simple query
            conn.createStatement().execute("SELECT 1");
            System.out.println("Test query executed successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Database connection test failed!");
            e.printStackTrace();
            return false;
        }
    }
}
-- Drop database if it exists and create new one
DROP DATABASE IF EXISTS your_database_name;
CREATE DATABASE your_database_name;
USE your_database_name;

-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert test user (optional)
INSERT INTO users (full_name, email, password)
VALUES ('Test User', 'test@test.com', 'password123');
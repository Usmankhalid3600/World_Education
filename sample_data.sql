-- Sample data for testing the Login Module
-- Run this script after the application creates the tables

-- Insert sample ADMIN user
-- User ID: admin001
-- Password: admin123 (SHA-256 hash)
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at)
VALUES ('admin001', 
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 
        0, 
        false, 
        'ADMIN', 
        NOW(), 
        NOW());

-- Insert sample STUDENT user
-- User ID: student001
-- Password: student123 (SHA-256 hash)
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at)
VALUES ('student001', 
        '6eae5be1b18564fc61ee7e2e38cf0d7c85d3fb3c7c5b3d5e9e0e8e7e8e7e8e7e', 
        0, 
        false, 
        'STUDENT', 
        NOW(), 
        NOW());

-- Insert sample STUDENT user (for testing account lock)
-- User ID: student002
-- Password: test123 (SHA-256 hash)
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at)
VALUES ('student002', 
        'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae', 
        0, 
        false, 
        'STUDENT', 
        NOW(), 
        NOW());

-- Get the customer_id values (assuming auto-increment starts at 1)
SET @admin_id = (SELECT customer_id FROM users WHERE user_id = 'admin001');
SET @student1_id = (SELECT customer_id FROM users WHERE user_id = 'student001');
SET @student2_id = (SELECT customer_id FROM users WHERE user_id = 'student002');

-- Insert user profiles
INSERT INTO users_profile (customer_id, first_name, middle_name, last_name, country, state, city, address, email, mobile_no, created_at, updated_at)
VALUES (@admin_id, 'John', 'A', 'Admin', 'USA', 'California', 'Los Angeles', '123 Admin St', 'admin@worldedu.com', '+1234567890', NOW(), NOW());

INSERT INTO users_profile (customer_id, first_name, middle_name, last_name, country, state, city, address, email, mobile_no, created_at, updated_at)
VALUES (@student1_id, 'Alice', 'B', 'Student', 'USA', 'New York', 'New York City', '456 Student Ave', 'alice@example.com', '+1234567891', NOW(), NOW());

INSERT INTO users_profile (customer_id, first_name, middle_name, last_name, country, state, city, address, email, mobile_no, created_at, updated_at)
VALUES (@student2_id, 'Bob', 'C', 'Student', 'USA', 'Texas', 'Houston', '789 Test Blvd', 'bob@example.com', '+1234567892', NOW(), NOW());

-- Sample test credentials:
-- ADMIN User:
--   User ID: admin001
--   Password: admin123
--
-- STUDENT Users:
--   User ID: student001
--   Password: student123
--
--   User ID: student002
--   Password: test123

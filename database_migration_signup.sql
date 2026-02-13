-- =====================================================
-- SQL Script: Update Users Table with signUp_method
-- =====================================================
-- Add signUp_method column to existing users table

-- Add column if it doesn't exist (safe migration)
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS signUp_method ENUM('DATA', 'GOOGLE') NOT NULL DEFAULT 'DATA' 
AFTER password_expiry;

-- Update existing users to have signUp_method = 'DATA'
UPDATE users 
SET signUp_method = 'DATA' 
WHERE signUp_method IS NULL;

-- =====================================================
-- Create code_verification table
-- =====================================================
CREATE TABLE IF NOT EXISTS code_verification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    secret_code VARCHAR(10) NOT NULL,
    action VARCHAR(50) NOT NULL,
    expiry_time DATETIME NOT NULL,
    generation_time DATETIME NOT NULL,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'LOGGED') NOT NULL DEFAULT 'ACTIVE',
    user_id VARCHAR(255) NOT NULL,
    INDEX idx_user_id_action (user_id, action),
    INDEX idx_status (status),
    INDEX idx_expiry_time (expiry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Sample Data for Testing
-- =====================================================

-- Sample verification codes (for testing)
INSERT INTO code_verification (secret_code, action, expiry_time, generation_time, status, user_id)
VALUES 
('123456', 'SIGNUP', DATE_ADD(NOW(), INTERVAL 15 MINUTE), NOW(), 'ACTIVE', 'test@example.com'),
('789012', 'SIGNUP', DATE_ADD(NOW(), INTERVAL 15 MINUTE), NOW(), 'ACTIVE', 'john@example.com'),
('456789', 'SIGNUP', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 'EXPIRED', 'expired@example.com');

-- Sample Google user (created via Google OAuth)
-- Note: This user has signUp_method = 'GOOGLE'
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, password_expiry, signUp_method)
VALUES 
('googleuser', '$2a$10$dummyHashForGoogleUser12345678901234567890123456', 0, FALSE, 'STUDENT', NULL, 'GOOGLE');

-- Get the customer_id of the Google user
SET @google_customer_id = LAST_INSERT_ID();

-- Create profile for Google user
INSERT INTO users_profile (customer_id, first_name, middle_name, last_name, country, state, city, address, email, mobile_no)
VALUES 
(@google_customer_id, 'Google', '', 'User', 'USA', 'California', 'San Francisco', '123 Google St', 'googleuser@gmail.com', '+14155551234');

-- =====================================================
-- Verification Queries
-- =====================================================

-- Check users table structure
DESCRIBE users;

-- Check code_verification table structure
DESCRIBE code_verification;

-- View all users with their signup method
SELECT 
    u.customer_id,
    u.user_id,
    u.user_category,
    u.signUp_method,
    u.account_locked,
    p.email,
    p.first_name,
    p.last_name
FROM users u
LEFT JOIN users_profile p ON u.customer_id = p.customer_id
ORDER BY u.customer_id DESC;

-- View active verification codes
SELECT 
    id,
    user_id,
    action,
    secret_code,
    status,
    generation_time,
    expiry_time,
    CASE 
        WHEN expiry_time > NOW() THEN 'VALID'
        ELSE 'EXPIRED'
    END as is_valid
FROM code_verification
WHERE status = 'ACTIVE'
ORDER BY generation_time DESC;

-- =====================================================
-- Cleanup Expired Codes (Run periodically)
-- =====================================================
-- This query should be run as a scheduled job to clean up expired codes

UPDATE code_verification 
SET status = 'EXPIRED' 
WHERE status = 'ACTIVE' 
AND expiry_time < NOW();

-- Optional: Delete old verification codes (older than 7 days)
-- DELETE FROM code_verification 
-- WHERE generation_time < DATE_SUB(NOW(), INTERVAL 7 DAY);

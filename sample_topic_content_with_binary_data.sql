-- =====================================================
-- Sample SQL Script: Topic Contents with Binary Data
-- =====================================================
-- This script shows how to insert topic content with binary data (BLOB)
-- Note: In production, binary data should be inserted via application code
-- This is for demonstration purposes only

-- First, ensure you have the sample data from previous scripts:
-- - sample_data.sql (users, classes, subjects, topics)
-- - sample_subscription_data.sql (subscriptions)

-- =====================================================
-- Method 1: Insert with NULL binary data (using file_path_url only)
-- =====================================================
-- For large files, you might store them on file system/cloud and only store the URL
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, topic_content_data, uploaded_by, uploaded_at, is_active)
VALUES 
(1, 'addition_basics.pdf', 'https://content.worldedu.com/math/addition/basics.pdf', 'PDF', NULL, 1, '2024-01-15 10:00:00', TRUE),
(1, 'addition_advanced.pdf', 'https://content.worldedu.com/math/addition/advanced.pdf', 'PDF', NULL, 1, '2024-01-16 11:00:00', TRUE);

-- =====================================================
-- Method 2: Insert with small text-based binary data (for demonstration)
-- =====================================================
-- For actual binary files, you would use LOAD_FILE() function in MySQL
-- or insert via application code (JDBC PreparedStatement with setBytes())

-- Example: Storing a small text file as BLOB
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, topic_content_data, uploaded_by, uploaded_at, is_active)
VALUES 
(2, 'subtraction_notes.txt', NULL, 'TXT', 
    CONVERT('This is a sample text content about subtraction. In mathematics, subtraction is the operation of finding the difference between two numbers.' USING utf8mb4),
    1, '2024-01-17 12:00:00', TRUE);

-- =====================================================
-- Method 3: Load file from disk (MySQL Server must have access to file)
-- =====================================================
-- Note: This requires FILE privilege and secure_file_priv setting
-- Example (commented out - requires actual file on server):
/*
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, topic_content_data, uploaded_by, uploaded_at, is_active)
VALUES 
(1, 'addition_worksheet.pdf', NULL, 'PDF', 
    LOAD_FILE('/path/to/addition_worksheet.pdf'),
    1, '2024-01-18 13:00:00', TRUE);
*/

-- =====================================================
-- Method 4: Using HEX string (for binary data)
-- =====================================================
-- You can insert binary data using HEX notation
-- Example: A simple 1x1 transparent PNG image
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, topic_content_data, uploaded_by, uploaded_at, is_active)
VALUES 
(3, 'sample_image.png', NULL, 'PNG',
    UNHEX('89504E470D0A1A0A0000000D49484452000000010000000108060000001F15C4890000000A49444154785E6300010000050001'), 
    1, '2024-01-19 14:00:00', TRUE);

-- =====================================================
-- Verify inserted data
-- =====================================================
SELECT 
    content_id,
    topic_id,
    file_name,
    file_type,
    CASE 
        WHEN topic_content_data IS NULL THEN 'NULL (using file_path_url)'
        ELSE CONCAT(LENGTH(topic_content_data), ' bytes')
    END as content_size,
    uploaded_at
FROM topic_contents
ORDER BY content_id;

-- =====================================================
-- Important Notes for Production:
-- =====================================================
-- 1. BLOB Size Limits:
--    - TINYBLOB: 255 bytes
--    - BLOB: 64 KB
--    - MEDIUMBLOB: 16 MB
--    - LONGBLOB: 4 GB
--    We're using LONGBLOB which supports up to 4GB files

-- 2. Best Practices:
--    - For files < 1MB: Store in database as BLOB
--    - For files > 1MB: Store on file system/cloud storage (S3, Azure Blob)
--      and only store the URL in database
--    - Always use PreparedStatement.setBytes() in Java to insert binary data
--    - Consider compression for large files before storage

-- 3. Performance Considerations:
--    - BLOBs can slow down table scans
--    - Consider separate table for BLOB data if needed
--    - Index on file_type, topic_id for faster queries
--    - Use streaming for large file downloads

-- 4. Security:
--    - Validate file types before upload
--    - Scan for malware/viruses
--    - Limit file size (e.g., max 50MB)
--    - Store uploaded_by for audit trails

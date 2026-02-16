-- ====================================================================
-- COMPLETE TEST DATA FOR WORLD EDUCATION APPLICATION
-- ====================================================================
-- This script creates comprehensive test data including:
-- 1. Admin and Student users
-- 2. Educational content (Classes, Subjects, Topics, Contents)
-- 3. Subscription plans (for classes, subjects, and topics)
-- 4. User subscriptions (opted plans)
-- 5. Payment records
-- ====================================================================

-- Clean up existing test data (optional - uncomment if needed)
-- DELETE FROM user_topic_subscriptions;
-- DELETE FROM user_subject_subscriptions;
-- DELETE FROM topic_contents;
-- DELETE FROM subscription_plans;
-- DELETE FROM ed_topics;
-- DELETE FROM ed_subjects;
-- DELETE FROM ed_classes;
-- DELETE FROM users_profile;
-- DELETE FROM users;

-- ====================================================================
-- SECTION 1: USERS & PROFILES
-- ====================================================================

-- Insert ADMIN User
-- User ID: admin001, Password: Admin@123
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at, signUp_method)
VALUES ('admin001', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
        0, 
        false, 
        'ADMIN', 
        NOW(), 
        NOW(),
        'DATA');

-- Insert STUDENT Users
-- User ID: student001, Password: Student@123
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at, signUp_method)
VALUES ('student001', 
        '$2a$10$8cjz47bjbR4Mn8GMjpb7COa8yT8FG5Dlg0ZwVjZmJ0xLCKp0/KQnq', 
        0, 
        false, 
        'STUDENT', 
        NOW(), 
        NOW(),
        'DATA');

-- User ID: student002, Password: Student@123
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at, signUp_method)
VALUES ('student002', 
        '$2a$10$8cjz47bjbR4Mn8GMjpb7COa8yT8FG5Dlg0ZwVjZmJ0xLCKp0/KQnq', 
        0, 
        false, 
        'STUDENT', 
        NOW(), 
        NOW(),
        'DATA');

-- User ID: student003, Password: Student@123
INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at, signUp_method)
VALUES ('student003', 
        '$2a$10$8cjz47bjbR4Mn8GMjpb7COa8yT8FG5Dlg0ZwVjZmJ0xLCKp0/KQnq', 
        0, 
        false, 
        'STUDENT', 
        NOW(), 
        NOW(),
        'DATA');

-- Get customer IDs
SET @admin_id = (SELECT customer_id FROM users WHERE user_id = 'admin001');
SET @student1_id = (SELECT customer_id FROM users WHERE user_id = 'student001');
SET @student2_id = (SELECT customer_id FROM users WHERE user_id = 'student002');
SET @student3_id = (SELECT customer_id FROM users WHERE user_id = 'student003');

-- Insert User Profiles
INSERT INTO users_profile (customer_id, first_name, middle_name, last_name, country, state, city, address, email, mobile_no, created_at, updated_at)
VALUES 
(@admin_id, 'Muhammad', 'Ali', 'Khan', 'Pakistan', 'Punjab', 'Lahore', 'DHA Phase 5, Block A', 'admin@worldedu.com', '+923001234567', NOW(), NOW()),
(@student1_id, 'Ahmed', 'Hassan', 'Raza', 'Pakistan', 'Sindh', 'Karachi', 'Clifton Block 8', 'ahmed.raza@example.com', '+923211234567', NOW(), NOW()),
(@student2_id, 'Fatima', 'Zahra', 'Sheikh', 'Pakistan', 'Punjab', 'Islamabad', 'F-7 Markaz', 'fatima.sheikh@example.com', '+923331234567', NOW(), NOW()),
(@student3_id, 'Usman', 'Abdullah', 'Malik', 'Pakistan', 'KPK', 'Peshawar', 'University Town', 'usman.malik@example.com', '+923451234567', NOW(), NOW());

-- ====================================================================
-- SECTION 2: EDUCATIONAL CONTENT HIERARCHY
-- ====================================================================

-- Insert Classes (Grades 1-10)
INSERT INTO ed_classes (class_name, class_number, is_active, created_at)
VALUES 
('Grade 1', 1, true, NOW()),
('Grade 2', 2, true, NOW()),
('Grade 3', 3, true, NOW()),
('Grade 4', 4, true, NOW()),
('Grade 5', 5, true, NOW()),
('Grade 6', 6, true, NOW()),
('Grade 7', 7, true, NOW()),
('Grade 8', 8, true, NOW()),
('Grade 9', 9, true, NOW()),
('Grade 10', 10, true, NOW());

-- Get Class IDs
SET @class1_id = (SELECT class_id FROM ed_classes WHERE class_number = 1);
SET @class2_id = (SELECT class_id FROM ed_classes WHERE class_number = 2);
SET @class3_id = (SELECT class_id FROM ed_classes WHERE class_number = 3);
SET @class5_id = (SELECT class_id FROM ed_classes WHERE class_number = 5);
SET @class8_id = (SELECT class_id FROM ed_classes WHERE class_number = 8);

-- Insert Subjects for Grade 1
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES 
(@class1_id, 'Mathematics', true, NOW()),
(@class1_id, 'English', true, NOW()),
(@class1_id, 'Urdu', true, NOW()),
(@class1_id, 'Science', true, NOW()),
(@class1_id, 'Islamiyat', true, NOW());

-- Insert Subjects for Grade 2
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES 
(@class2_id, 'Mathematics', true, NOW()),
(@class2_id, 'English', true, NOW()),
(@class2_id, 'Urdu', true, NOW()),
(@class2_id, 'General Science', true, NOW()),
(@class2_id, 'Social Studies', true, NOW());

-- Insert Subjects for Grade 5
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES 
(@class5_id, 'Mathematics', true, NOW()),
(@class5_id, 'English', true, NOW()),
(@class5_id, 'Physics', true, NOW()),
(@class5_id, 'Chemistry', true, NOW()),
(@class5_id, 'Biology', true, NOW()),
(@class5_id, 'Computer Science', true, NOW());

-- Insert Subjects for Grade 8
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES 
(@class8_id, 'Mathematics', true, NOW()),
(@class8_id, 'Physics', true, NOW()),
(@class8_id, 'Chemistry', true, NOW()),
(@class8_id, 'Biology', true, NOW()),
(@class8_id, 'English', true, NOW()),
(@class8_id, 'Computer Science', true, NOW());

-- Get Subject IDs for Grade 1
SET @g1_math_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class1_id AND subject_name = 'Mathematics');
SET @g1_english_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class1_id AND subject_name = 'English');
SET @g1_urdu_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class1_id AND subject_name = 'Urdu');
SET @g1_science_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class1_id AND subject_name = 'Science');

-- Get Subject IDs for Grade 5
SET @g5_math_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class5_id AND subject_name = 'Mathematics');
SET @g5_physics_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class5_id AND subject_name = 'Physics');
SET @g5_chemistry_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class5_id AND subject_name = 'Chemistry');
SET @g5_cs_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class5_id AND subject_name = 'Computer Science');

-- Get Subject IDs for Grade 8
SET @g8_math_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class8_id AND subject_name = 'Mathematics');
SET @g8_physics_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class8_id AND subject_name = 'Physics');

-- Insert Topics for Grade 1 Mathematics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g1_math_id, 'Counting 1 to 100', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g1_math_id, 'Addition Basics', DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g1_math_id, 'Subtraction Basics', DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g1_math_id, 'Shapes and Patterns', DATE_SUB(NOW(), INTERVAL 15 DAY), true, NOW()),
(@g1_math_id, 'Measurement Basics', DATE_SUB(NOW(), INTERVAL 10 DAY), true, NOW());

-- Insert Topics for Grade 1 English
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g1_english_id, 'Alphabets A-Z', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g1_english_id, 'Phonics and Sounds', DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g1_english_id, 'Simple Words', DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g1_english_id, 'Reading Short Stories', DATE_SUB(NOW(), INTERVAL 15 DAY), true, NOW());

-- Insert Topics for Grade 1 Science
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g1_science_id, 'Living Things', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g1_science_id, 'Plants and Animals', DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g1_science_id, 'Water and Air', DATE_SUB(NOW(), INTERVAL 10 DAY), true, NOW());

-- Insert Topics for Grade 5 Mathematics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g5_math_id, 'Fractions and Decimals', DATE_SUB(NOW(), INTERVAL 40 DAY), true, NOW()),
(@g5_math_id, 'Algebra Basics', DATE_SUB(NOW(), INTERVAL 35 DAY), true, NOW()),
(@g5_math_id, 'Geometry - Angles and Triangles', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g5_math_id, 'Percentages', DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g5_math_id, 'Data Handling', DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW());

-- Insert Topics for Grade 5 Physics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g5_physics_id, 'Force and Motion', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g5_physics_id, 'Energy Types', DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g5_physics_id, 'Light and Sound', DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g5_physics_id, 'Electricity Basics', DATE_SUB(NOW(), INTERVAL 15 DAY), true, NOW());

-- Insert Topics for Grade 5 Computer Science
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g5_cs_id, 'Introduction to Computers', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g5_cs_id, 'MS Office Basics', DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g5_cs_id, 'Internet and Email', DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g5_cs_id, 'Basic Programming Concepts', DATE_SUB(NOW(), INTERVAL 15 DAY), true, NOW());

-- Insert Topics for Grade 8 Mathematics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g8_math_id, 'Quadratic Equations', DATE_SUB(NOW(), INTERVAL 40 DAY), true, NOW()),
(@g8_math_id, 'Trigonometry Basics', DATE_SUB(NOW(), INTERVAL 35 DAY), true, NOW()),
(@g8_math_id, 'Statistics and Probability', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW());

-- Insert Topics for Grade 8 Physics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@g8_physics_id, 'Newtons Laws of Motion', DATE_SUB(NOW(), INTERVAL 40 DAY), true, NOW()),
(@g8_physics_id, 'Work, Power and Energy', DATE_SUB(NOW(), INTERVAL 35 DAY), true, NOW()),
(@g8_physics_id, 'Waves and Sound', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW());

-- Get some Topic IDs for later use
SET @topic_counting = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Counting 1 to 100');
SET @topic_addition = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Addition Basics');
SET @topic_alphabets = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Alphabets A-Z');
SET @topic_phonics = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Phonics and Sounds');
SET @topic_fractions = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Fractions and Decimals');
SET @topic_algebra = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Algebra Basics');
SET @topic_force = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Force and Motion');

-- Insert Sample Topic Content
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, uploaded_by, uploaded_at, is_active)
VALUES 
(@topic_counting, 'counting_1_to_100.pdf', '/content/grade1/math/counting_1_to_100.pdf', 'PDF', @admin_id, NOW(), true),
(@topic_addition, 'addition_basics.pdf', '/content/grade1/math/addition_basics.pdf', 'PDF', @admin_id, NOW(), true),
(@topic_addition, 'addition_practice.pdf', '/content/grade1/math/addition_practice.pdf', 'PDF', @admin_id, NOW(), true),
(@topic_alphabets, 'alphabets_az.pdf', '/content/grade1/english/alphabets_az.pdf', 'PDF', @admin_id, NOW(), true),
(@topic_fractions, 'fractions_decimals.pdf', '/content/grade5/math/fractions_decimals.pdf', 'PDF', @admin_id, NOW(), true),
(@topic_algebra, 'algebra_basics.pdf', '/content/grade5/math/algebra_basics.pdf', 'PDF', @admin_id, NOW(), true);

-- ====================================================================
-- SECTION 3: SUBSCRIPTION PLANS
-- ====================================================================

-- Subscription Plans for SUBJECTS (Grade 1)
INSERT INTO subscription_plans (plan_name, target_type, target_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES 
('Grade 1 Mathematics - Monthly', 'SUBJECT', @g1_math_id, 30, 500.00, 'PKR', 3, 7, true, NOW(), NOW()),
('Grade 1 Mathematics - Quarterly', 'SUBJECT', @g1_math_id, 90, 1350.00, 'PKR', 5, 0, true, NOW(), NOW()),
('Grade 1 English - Monthly', 'SUBJECT', @g1_english_id, 30, 450.00, 'PKR', 3, 7, true, NOW(), NOW()),
('Grade 1 Science - Monthly', 'SUBJECT', @g1_science_id, 30, 400.00, 'PKR', 3, 5, true, NOW(), NOW()),
('Grade 1 Urdu - Monthly', 'SUBJECT', @g1_urdu_id, 30, 350.00, 'PKR', 3, 7, true, NOW(), NOW());

-- Subscription Plans for SUBJECTS (Grade 5)
INSERT INTO subscription_plans (plan_name, target_type, target_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES 
('Grade 5 Mathematics - Monthly', 'SUBJECT', @g5_math_id, 30, 800.00, 'PKR', 3, 0, true, NOW(), NOW()),
('Grade 5 Physics - Monthly', 'SUBJECT', @g5_physics_id, 30, 750.00, 'PKR', 3, 5, true, NOW(), NOW()),
('Grade 5 Chemistry - Monthly', 'SUBJECT', @g5_chemistry_id, 30, 750.00, 'PKR', 3, 5, true, NOW(), NOW()),
('Grade 5 Computer Science - Monthly', 'SUBJECT', @g5_cs_id, 30, 900.00, 'PKR', 3, 7, true, NOW(), NOW());

-- Subscription Plans for SUBJECTS (Grade 8)
INSERT INTO subscription_plans (plan_name, target_type, target_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES 
('Grade 8 Mathematics - Monthly', 'SUBJECT', @g8_math_id, 30, 1000.00, 'PKR', 5, 0, true, NOW(), NOW()),
('Grade 8 Physics - Monthly', 'SUBJECT', @g8_physics_id, 30, 950.00, 'PKR', 5, 0, true, NOW(), NOW());

-- Subscription Plans for TOPICS
INSERT INTO subscription_plans (plan_name, target_type, target_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES 
('Counting 1 to 100 - Monthly', 'TOPIC', @topic_counting, 30, 150.00, 'PKR', 2, 3, true, NOW(), NOW()),
('Addition Basics - Monthly', 'TOPIC', @topic_addition, 30, 150.00, 'PKR', 2, 3, true, NOW(), NOW()),
('Alphabets A-Z - Monthly', 'TOPIC', @topic_alphabets, 30, 120.00, 'PKR', 2, 5, true, NOW(), NOW()),
('Phonics and Sounds - Monthly', 'TOPIC', @topic_phonics, 30, 120.00, 'PKR', 2, 5, true, NOW(), NOW()),
('Fractions and Decimals - Monthly', 'TOPIC', @topic_fractions, 30, 200.00, 'PKR', 3, 0, true, NOW(), NOW()),
('Algebra Basics - Monthly', 'TOPIC', @topic_algebra, 30, 250.00, 'PKR', 3, 0, true, NOW(), NOW()),
('Force and Motion - Monthly', 'TOPIC', @topic_force, 30, 200.00, 'PKR', 3, 5, true, NOW(), NOW());

-- Subscription Plans for CLASSES (Full Grade Access)
INSERT INTO subscription_plans (plan_name, target_type, target_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES 
('Grade 1 - Full Access Monthly', 'CLASS', @class1_id, 30, 1500.00, 'PKR', 5, 14, true, NOW(), NOW()),
('Grade 1 - Full Access Yearly', 'CLASS', @class1_id, 365, 15000.00, 'PKR', 10, 0, true, NOW(), NOW()),
('Grade 5 - Full Access Monthly', 'CLASS', @class5_id, 30, 3000.00, 'PKR', 5, 7, true, NOW(), NOW()),
('Grade 8 - Full Access Monthly', 'CLASS', @class8_id, 30, 3500.00, 'PKR', 5, 0, true, NOW(), NOW());

-- ====================================================================
-- SECTION 4: USER SUBSCRIPTIONS (OPTED SUBJECTS & TOPICS)
-- ====================================================================

-- Student1 subscriptions to SUBJECTS
INSERT INTO user_subject_subscriptions (customer_id, subject_id, subscribed_at, is_active)
VALUES 
(@student1_id, @g1_math_id, DATE_SUB(NOW(), INTERVAL 15 DAY), true),
(@student1_id, @g1_english_id, DATE_SUB(NOW(), INTERVAL 10 DAY), true);

-- Student2 subscriptions to SUBJECTS
INSERT INTO user_subject_subscriptions (customer_id, subject_id, subscribed_at, is_active)
VALUES 
(@student2_id, @g5_math_id, DATE_SUB(NOW(), INTERVAL 20 DAY), true),
(@student2_id, @g5_physics_id, DATE_SUB(NOW(), INTERVAL 15 DAY), true),
(@student2_id, @g5_cs_id, DATE_SUB(NOW(), INTERVAL 10 DAY), true);

-- Student3 subscriptions to SUBJECTS
INSERT INTO user_subject_subscriptions (customer_id, subject_id, subscribed_at, is_active)
VALUES 
(@student3_id, @g8_math_id, DATE_SUB(NOW(), INTERVAL 25 DAY), true),
(@student3_id, @g8_physics_id, DATE_SUB(NOW(), INTERVAL 20 DAY), true);

-- Student1 subscriptions to TOPICS
INSERT INTO user_topic_subscriptions (customer_id, topic_id, subscribed_at, is_active)
VALUES 
(@student1_id, @topic_counting, DATE_SUB(NOW(), INTERVAL 14 DAY), true),
(@student1_id, @topic_addition, DATE_SUB(NOW(), INTERVAL 12 DAY), true),
(@student1_id, @topic_alphabets, DATE_SUB(NOW(), INTERVAL 9 DAY), true);

-- Student2 subscriptions to TOPICS
INSERT INTO user_topic_subscriptions (customer_id, topic_id, subscribed_at, is_active)
VALUES 
(@student2_id, @topic_fractions, DATE_SUB(NOW(), INTERVAL 18 DAY), true),
(@student2_id, @topic_algebra, DATE_SUB(NOW(), INTERVAL 15 DAY), true),
(@student2_id, @topic_force, DATE_SUB(NOW(), INTERVAL 12 DAY), true);

-- Student3 subscriptions to TOPICS (has subject subscription but also specific topics)
INSERT INTO user_topic_subscriptions (customer_id, topic_id, subscribed_at, is_active)
VALUES 
(@student3_id, @topic_counting, DATE_SUB(NOW(), INTERVAL 5 DAY), true),
(@student3_id, @topic_phonics, DATE_SUB(NOW(), INTERVAL 3 DAY), true);

-- ====================================================================
-- DATA SUMMARY
-- ====================================================================

-- Display summary information
SELECT '====================================================================';
SELECT 'TEST DATA LOADED SUCCESSFULLY!';
SELECT '====================================================================';
SELECT '';
SELECT 'USERS CREATED:';
SELECT '  Admin:';
SELECT '    User ID: admin001';
SELECT '    Password: Admin@123';
SELECT '    Email: admin@worldedu.com';
SELECT '';
SELECT '  Students:';
SELECT '    User ID: student001, Password: Student@123, Email: ahmed.raza@example.com';
SELECT '    User ID: student002, Password: Student@123, Email: fatima.sheikh@example.com';
SELECT '    User ID: student003, Password: Student@123, Email: usman.malik@example.com';
SELECT '';
SELECT 'EDUCATIONAL CONTENT:';
SELECT CONCAT('  Classes: ', (SELECT COUNT(*) FROM ed_classes), ' grades');
SELECT CONCAT('  Subjects: ', (SELECT COUNT(*) FROM ed_subjects), ' subjects across all grades');
SELECT CONCAT('  Topics: ', (SELECT COUNT(*) FROM ed_topics), ' topics');
SELECT CONCAT('  Contents: ', (SELECT COUNT(*) FROM topic_contents), ' content files');
SELECT '';
SELECT 'SUBSCRIPTION PLANS:';
SELECT CONCAT('  Total Plans: ', (SELECT COUNT(*) FROM subscription_plans));
SELECT CONCAT('  Subject Plans: ', (SELECT COUNT(*) FROM subscription_plans WHERE target_type = 'SUBJECT'));
SELECT CONCAT('  Topic Plans: ', (SELECT COUNT(*) FROM subscription_plans WHERE target_type = 'TOPIC'));
SELECT CONCAT('  Class Plans: ', (SELECT COUNT(*) FROM subscription_plans WHERE target_type = 'CLASS'));
SELECT '';
SELECT 'USER SUBSCRIPTIONS:';
SELECT CONCAT('  Subject Subscriptions: ', (SELECT COUNT(*) FROM user_subject_subscriptions));
SELECT CONCAT('  Topic Subscriptions: ', (SELECT COUNT(*) FROM user_topic_subscriptions));
SELECT '';
SELECT 'STUDENT SUBSCRIPTION DETAILS:';
SELECT '';
SELECT 'student001 (Ahmed):';
SELECT CONCAT('  - Opted Subjects: ', (SELECT COUNT(*) FROM user_subject_subscriptions WHERE customer_id = @student1_id));
SELECT CONCAT('  - Opted Topics: ', (SELECT COUNT(*) FROM user_topic_subscriptions WHERE customer_id = @student1_id));
SELECT '';
SELECT 'student002 (Fatima):';
SELECT CONCAT('  - Opted Subjects: ', (SELECT COUNT(*) FROM user_subject_subscriptions WHERE customer_id = @student2_id));
SELECT CONCAT('  - Opted Topics: ', (SELECT COUNT(*) FROM user_topic_subscriptions WHERE customer_id = @student2_id));
SELECT '';
SELECT 'student003 (Usman):';
SELECT CONCAT('  - Opted Subjects: ', (SELECT COUNT(*) FROM user_subject_subscriptions WHERE customer_id = @student3_id));
SELECT CONCAT('  - Opted Topics: ', (SELECT COUNT(*) FROM user_topic_subscriptions WHERE customer_id = @student3_id));
SELECT '';
SELECT '====================================================================';
SELECT 'You can now test the following scenarios:';
SELECT '  1. Login as admin and manage content';
SELECT '  2. Login as students and view opted/unopted subjects/topics';
SELECT '  3. Test subscription plans and pricing display';
SELECT '  4. Test search functionality with topics';
SELECT '  5. Test admin endpoints with ADMIN role authorization';
SELECT '  6. Test student endpoints with STUDENT role authorization';
SELECT '====================================================================';

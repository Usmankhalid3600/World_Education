-- ====================================================================
-- WORLD EDUCATION — CLEAN SEED DATA
-- ====================================================================
-- Run this script to wipe all existing data and load fresh test data.
-- 3 Classes | Subjects per class | Topics per subject
-- 1 Admin   | 2 Students
-- ====================================================================

-- ====================================================================
-- SECTION 0: SCHEMA FIX + CLEAN UP
-- ====================================================================

-- Fix subscription_plans table: remove legacy target_id column if it exists,
-- and ensure the typed FK columns are present (Hibernate ddl-auto=update
-- adds new columns but never drops old ones).
ALTER TABLE subscription_plans
    MODIFY COLUMN class_id   BIGINT NULL,
    MODIFY COLUMN subject_id BIGINT NULL,
    MODIFY COLUMN topic_id   BIGINT NULL;

ALTER TABLE subscription_plans
    DROP COLUMN IF EXISTS target_id;

-- Add is_free column to topic_contents if it doesn't exist yet
-- (Hibernate ddl-auto=update will add it on first boot, but this ensures
--  the seed script can run before the app restarts.)
ALTER TABLE topic_contents
    ADD COLUMN IF NOT EXISTS is_free TINYINT(1) NOT NULL DEFAULT 0;

-- Add description column to ed_classes, ed_subjects, ed_topics
ALTER TABLE ed_classes
    ADD COLUMN IF NOT EXISTS description TEXT NULL;

ALTER TABLE ed_subjects
    ADD COLUMN IF NOT EXISTS description TEXT NULL;

ALTER TABLE ed_topics
    ADD COLUMN IF NOT EXISTS description TEXT NULL;

-- Delete all data in FK-safe order (children before parents)
DELETE FROM user_topic_subscriptions;
DELETE FROM user_subject_subscriptions;
DELETE FROM topic_contents;
DELETE FROM subscription_plan_history;
DELETE FROM subscription_plans;
DELETE FROM ed_topics;
DELETE FROM ed_subjects;
DELETE FROM ed_classes;
DELETE FROM user_sessions;
DELETE FROM code_verification;
DELETE FROM users_profile;
DELETE FROM users;

-- ====================================================================
-- SECTION 1: USERS
-- ====================================================================
-- All passwords = Admin@123
-- Hash: 4f40a2075276602aba5c74e5ab3dc98bf00d140c800f82b2aa828e4692587f18

INSERT INTO users (user_id, password_hash, failed_login_attempts, account_locked, user_category, created_at, updated_at, signUp_method)
VALUES
('admin001',   '4f40a2075276602aba5c74e5ab3dc98bf00d140c800f82b2aa828e4692587f18', 0, false, 'ADMIN',   NOW(), NOW(), 'DATA'),
('student001', '4f40a2075276602aba5c74e5ab3dc98bf00d140c800f82b2aa828e4692587f18', 0, false, 'STUDENT', NOW(), NOW(), 'DATA'),
('student002', '4f40a2075276602aba5c74e5ab3dc98bf00d140c800f82b2aa828e4692587f18', 0, false, 'STUDENT', NOW(), NOW(), 'DATA');

SET @admin_id    = (SELECT customer_id FROM users WHERE user_id = 'admin001');
SET @student1_id = (SELECT customer_id FROM users WHERE user_id = 'student001');
SET @student2_id = (SELECT customer_id FROM users WHERE user_id = 'student002');

INSERT INTO users_profile (customer_id, first_name, middle_name, last_name, country, state, city, address, email, mobile_no, created_at, updated_at)
VALUES
(@admin_id,    'Ali',    NULL,      'Khan',   'Pakistan', 'Punjab', 'Lahore',    'DHA Phase 5',   'admin@worldedu.com',    '+923001234567', NOW(), NOW()),
(@student1_id, 'Ahmed',  'Hassan',  'Raza',   'Pakistan', 'Sindh',  'Karachi',   'Clifton Block 8','ahmed@example.com',    '+923211234567', NOW(), NOW()),
(@student2_id, 'Fatima', 'Zahra',   'Sheikh', 'Pakistan', 'Punjab', 'Islamabad', 'F-7 Markaz',    'fatima@example.com',   '+923331234567', NOW(), NOW());

-- ====================================================================
-- SECTION 2: CLASSES
-- ====================================================================

INSERT INTO ed_classes (class_name, class_number, is_active, created_at)
VALUES
('Grade 5',  5,  true, NOW()),
('Grade 8',  8,  true, NOW()),
('Grade 10', 10, true, NOW());

SET @class5_id  = (SELECT class_id FROM ed_classes WHERE class_number = 5);
SET @class8_id  = (SELECT class_id FROM ed_classes WHERE class_number = 8);
SET @class10_id = (SELECT class_id FROM ed_classes WHERE class_number = 10);

-- ====================================================================
-- SECTION 3: SUBJECTS
-- ====================================================================

-- Grade 5 subjects
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES
(@class5_id, 'Mathematics',     true, NOW()),
(@class5_id, 'English',         true, NOW()),
(@class5_id, 'General Science', true, NOW());

-- Grade 8 subjects
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES
(@class8_id, 'Mathematics', true, NOW()),
(@class8_id, 'Physics',     true, NOW()),
(@class8_id, 'Chemistry',   true, NOW()),
(@class8_id, 'English',     true, NOW());

-- Grade 10 subjects
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES
(@class10_id, 'Mathematics',    true, NOW()),
(@class10_id, 'Physics',        true, NOW()),
(@class10_id, 'English',        true, NOW()),
(@class10_id, 'Computer Science', true, NOW());

-- Resolve subject IDs
SET @g5_math_id    = (SELECT subject_id FROM ed_subjects WHERE class_id = @class5_id  AND subject_name = 'Mathematics');
SET @g5_eng_id     = (SELECT subject_id FROM ed_subjects WHERE class_id = @class5_id  AND subject_name = 'English');
SET @g5_sci_id     = (SELECT subject_id FROM ed_subjects WHERE class_id = @class5_id  AND subject_name = 'General Science');

SET @g8_math_id    = (SELECT subject_id FROM ed_subjects WHERE class_id = @class8_id  AND subject_name = 'Mathematics');
SET @g8_physics_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class8_id  AND subject_name = 'Physics');
SET @g8_chem_id    = (SELECT subject_id FROM ed_subjects WHERE class_id = @class8_id  AND subject_name = 'Chemistry');
SET @g8_eng_id     = (SELECT subject_id FROM ed_subjects WHERE class_id = @class8_id  AND subject_name = 'English');

SET @g10_math_id   = (SELECT subject_id FROM ed_subjects WHERE class_id = @class10_id AND subject_name = 'Mathematics');
SET @g10_phys_id   = (SELECT subject_id FROM ed_subjects WHERE class_id = @class10_id AND subject_name = 'Physics');
SET @g10_eng_id    = (SELECT subject_id FROM ed_subjects WHERE class_id = @class10_id AND subject_name = 'English');
SET @g10_cs_id     = (SELECT subject_id FROM ed_subjects WHERE class_id = @class10_id AND subject_name = 'Computer Science');

-- ====================================================================
-- SECTION 4: TOPICS
-- ====================================================================

-- Grade 5 Mathematics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g5_math_id, 'Fractions and Decimals',         DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g5_math_id, 'Percentages',                    DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g5_math_id, 'Geometry – Angles and Triangles',DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g5_math_id, 'Data Handling',                  DATE_SUB(NOW(), INTERVAL 15 DAY), true, NOW());

-- Grade 5 English
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g5_eng_id, 'Reading Comprehension',   DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g5_eng_id, 'Essay Writing',           DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g5_eng_id, 'Grammar – Tenses',        DATE_SUB(NOW(), INTERVAL 10 DAY), true, NOW());

-- Grade 5 General Science
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g5_sci_id, 'Living and Non-Living Things', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g5_sci_id, 'Human Body Systems',           DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW()),
(@g5_sci_id, 'Forces and Simple Machines',   DATE_SUB(NOW(), INTERVAL 10 DAY), true, NOW());

-- Grade 8 Mathematics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g8_math_id, 'Linear Equations',        DATE_SUB(NOW(), INTERVAL 40 DAY), true, NOW()),
(@g8_math_id, 'Quadratic Equations',     DATE_SUB(NOW(), INTERVAL 35 DAY), true, NOW()),
(@g8_math_id, 'Trigonometry Basics',     DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g8_math_id, 'Statistics',              DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW());

-- Grade 8 Physics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g8_physics_id, 'Newton\'s Laws of Motion', DATE_SUB(NOW(), INTERVAL 40 DAY), true, NOW()),
(@g8_physics_id, 'Work, Power and Energy',   DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g8_physics_id, 'Waves and Sound',          DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW());

-- Grade 8 Chemistry
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g8_chem_id, 'Atomic Structure',     DATE_SUB(NOW(), INTERVAL 35 DAY), true, NOW()),
(@g8_chem_id, 'Chemical Bonding',     DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g8_chem_id, 'Acids and Bases',      DATE_SUB(NOW(), INTERVAL 15 DAY), true, NOW());

-- Grade 8 English
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g8_eng_id, 'Advanced Essay Writing', DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g8_eng_id, 'Poetry Analysis',        DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW());

-- Grade 10 Mathematics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g10_math_id, 'Calculus – Derivatives',  DATE_SUB(NOW(), INTERVAL 45 DAY), true, NOW()),
(@g10_math_id, 'Integration Basics',      DATE_SUB(NOW(), INTERVAL 35 DAY), true, NOW()),
(@g10_math_id, 'Matrices',                DATE_SUB(NOW(), INTERVAL 25 DAY), true, NOW()),
(@g10_math_id, 'Probability',             DATE_SUB(NOW(), INTERVAL 15 DAY), true, NOW());

-- Grade 10 Physics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g10_phys_id, 'Electromagnetism',    DATE_SUB(NOW(), INTERVAL 40 DAY), true, NOW()),
(@g10_phys_id, 'Modern Physics',      DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g10_phys_id, 'Optics',              DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW());

-- Grade 10 Computer Science
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES
(@g10_cs_id, 'Programming in C++',      DATE_SUB(NOW(), INTERVAL 40 DAY), true, NOW()),
(@g10_cs_id, 'Data Structures Basics',  DATE_SUB(NOW(), INTERVAL 30 DAY), true, NOW()),
(@g10_cs_id, 'Database Fundamentals',   DATE_SUB(NOW(), INTERVAL 20 DAY), true, NOW());

-- ====================================================================
-- SECTION 5: SUBSCRIPTION PLANS
-- ====================================================================

-- ── CLASS plans ──────────────────────────────────────────────────────
INSERT INTO subscription_plans (plan_name, target_type, class_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES
('Grade 5 – Full Access Monthly',  'CLASS', @class5_id,  30, 2500.00, 'PKR', 5, 7, true, NOW(), NOW()),
('Grade 8 – Full Access Monthly',  'CLASS', @class8_id,  30, 3000.00, 'PKR', 5, 5, true, NOW(), NOW()),
('Grade 10 – Full Access Monthly', 'CLASS', @class10_id, 30, 3500.00, 'PKR', 5, 0, true, NOW(), NOW());

-- ── SUBJECT plans ────────────────────────────────────────────────────
INSERT INTO subscription_plans (plan_name, target_type, subject_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES
-- Grade 5
('Grade 5 Mathematics – Monthly',    'SUBJECT', @g5_math_id,    30,  800.00, 'PKR', 3, 7, true, NOW(), NOW()),
('Grade 5 English – Monthly',        'SUBJECT', @g5_eng_id,     30,  700.00, 'PKR', 3, 7, true, NOW(), NOW()),
('Grade 5 General Science – Monthly','SUBJECT', @g5_sci_id,     30,  700.00, 'PKR', 3, 5, true, NOW(), NOW()),
-- Grade 8
('Grade 8 Mathematics – Monthly',    'SUBJECT', @g8_math_id,    30, 1000.00, 'PKR', 3, 0, true, NOW(), NOW()),
('Grade 8 Physics – Monthly',        'SUBJECT', @g8_physics_id, 30,  950.00, 'PKR', 3, 0, true, NOW(), NOW()),
('Grade 8 Chemistry – Monthly',      'SUBJECT', @g8_chem_id,    30,  950.00, 'PKR', 3, 0, true, NOW(), NOW()),
('Grade 8 English – Monthly',        'SUBJECT', @g8_eng_id,     30,  800.00, 'PKR', 3, 5, true, NOW(), NOW()),
-- Grade 10
('Grade 10 Mathematics – Monthly',   'SUBJECT', @g10_math_id,   30, 1200.00, 'PKR', 5, 0, true, NOW(), NOW()),
('Grade 10 Physics – Monthly',       'SUBJECT', @g10_phys_id,   30, 1150.00, 'PKR', 5, 0, true, NOW(), NOW()),
('Grade 10 Computer Science – Monthly','SUBJECT',@g10_cs_id,    30, 1100.00, 'PKR', 5, 0, true, NOW(), NOW());

-- ── TOPIC plans ──────────────────────────────────────────────────────
-- (a selection of key topics)

SET @t_fractions  = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Fractions and Decimals');
SET @t_quadratic  = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Quadratic Equations');
SET @t_newton     = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Newton\'s Laws of Motion');
SET @t_calculus   = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Calculus – Derivatives');
SET @t_cpp        = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Programming in C++');

INSERT INTO subscription_plans (plan_name, target_type, topic_id, duration_days, price, currency, grace_period_days, free_days, is_active, created_at, updated_at)
VALUES
('Fractions and Decimals – Monthly', 'TOPIC', @t_fractions, 30, 200.00, 'PKR', 2, 3, true, NOW(), NOW()),
('Quadratic Equations – Monthly',    'TOPIC', @t_quadratic, 30, 250.00, 'PKR', 2, 0, true, NOW(), NOW()),
('Newton\'s Laws – Monthly',         'TOPIC', @t_newton,    30, 250.00, 'PKR', 2, 3, true, NOW(), NOW()),
('Calculus Derivatives – Monthly',   'TOPIC', @t_calculus,  30, 300.00, 'PKR', 3, 0, true, NOW(), NOW()),
('Programming in C++ – Monthly',     'TOPIC', @t_cpp,       30, 300.00, 'PKR', 3, 0, true, NOW(), NOW());

-- ====================================================================
-- SECTION 6: STUDENT SUBSCRIPTIONS
-- ====================================================================

-- student001 → subscribed to Grade 5 Maths and General Science subjects
INSERT INTO user_subject_subscriptions (customer_id, subject_id, subscribed_at, is_active)
VALUES
(@student1_id, @g5_math_id, DATE_SUB(NOW(), INTERVAL 10 DAY), true),
(@student1_id, @g5_sci_id,  DATE_SUB(NOW(), INTERVAL  7 DAY), true);

-- student001 → also subscribed to specific topic
INSERT INTO user_topic_subscriptions (customer_id, topic_id, subscribed_at, is_active)
VALUES
(@student1_id, @t_fractions, DATE_SUB(NOW(), INTERVAL 9 DAY), true);

-- student002 → subscribed to Grade 8 Physics and Grade 10 CS subjects
INSERT INTO user_subject_subscriptions (customer_id, subject_id, subscribed_at, is_active)
VALUES
(@student2_id, @g8_physics_id, DATE_SUB(NOW(), INTERVAL 15 DAY), true),
(@student2_id, @g10_cs_id,     DATE_SUB(NOW(), INTERVAL 10 DAY), true);

-- student002 → also subscribed to specific topics
INSERT INTO user_topic_subscriptions (customer_id, topic_id, subscribed_at, is_active)
VALUES
(@student2_id, @t_newton,  DATE_SUB(NOW(), INTERVAL 14 DAY), true),
(@student2_id, @t_cpp,     DATE_SUB(NOW(), INTERVAL  9 DAY), true);

-- ====================================================================
-- SECTION 7: SUMMARY
-- ====================================================================

SELECT '=== SEED DATA LOADED SUCCESSFULLY ===' AS status;

SELECT CONCAT('Users:    ', COUNT(*)) AS info FROM users;
SELECT CONCAT('Classes:  ', COUNT(*)) AS info FROM ed_classes;
SELECT CONCAT('Subjects: ', COUNT(*)) AS info FROM ed_subjects;
SELECT CONCAT('Topics:   ', COUNT(*)) AS info FROM ed_topics;
SELECT CONCAT('Plans:    ', COUNT(*)) AS info FROM subscription_plans;
SELECT CONCAT('  CLASS plans:   ', COUNT(*)) AS info FROM subscription_plans WHERE target_type = 'CLASS';
SELECT CONCAT('  SUBJECT plans: ', COUNT(*)) AS info FROM subscription_plans WHERE target_type = 'SUBJECT';
SELECT CONCAT('  TOPIC plans:   ', COUNT(*)) AS info FROM subscription_plans WHERE target_type = 'TOPIC';

SELECT '=== CREDENTIALS ===' AS info;
SELECT 'admin001   / Admin@123  (ADMIN)'   AS credentials;
SELECT 'student001 / Admin@123  (STUDENT)' AS credentials;
SELECT 'student002 / Admin@123  (STUDENT)' AS credentials;

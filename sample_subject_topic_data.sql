-- Sample data for testing Subject and Topic modules

-- Insert sample classes
INSERT INTO ed_classes (class_name, class_number, is_active, created_at)
VALUES 
('Grade 1', 1, true, NOW()),
('Grade 2', 2, true, NOW()),
('Grade 3', 3, true, NOW()),
('Grade 4', 4, true, NOW()),
('Grade 5', 5, true, NOW());

-- Get class IDs
SET @class1_id = (SELECT class_id FROM ed_classes WHERE class_number = 1);
SET @class2_id = (SELECT class_id FROM ed_classes WHERE class_number = 2);

-- Insert sample subjects for Grade 1
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES 
(@class1_id, 'Mathematics', true, NOW()),
(@class1_id, 'English', true, NOW()),
(@class1_id, 'Science', true, NOW()),
(@class1_id, 'Social Studies', true, NOW()),
(@class1_id, 'Art', true, NOW());

-- Insert sample subjects for Grade 2
INSERT INTO ed_subjects (class_id, subject_name, is_active, created_at)
VALUES 
(@class2_id, 'Mathematics', true, NOW()),
(@class2_id, 'English', true, NOW()),
(@class2_id, 'Science', true, NOW()),
(@class2_id, 'Computer Science', true, NOW()),
(@class2_id, 'Physical Education', true, NOW());

-- Get subject IDs for Grade 1
SET @math_subject_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class1_id AND subject_name = 'Mathematics');
SET @english_subject_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class1_id AND subject_name = 'English');
SET @science_subject_id = (SELECT subject_id FROM ed_subjects WHERE class_id = @class1_id AND subject_name = 'Science');

-- Insert sample topics for Mathematics
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@math_subject_id, 'Addition and Subtraction', NOW(), true, NOW()),
(@math_subject_id, 'Multiplication Basics', NOW(), true, NOW()),
(@math_subject_id, 'Division Basics', NOW(), true, NOW()),
(@math_subject_id, 'Fractions Introduction', NOW(), true, NOW());

-- Insert sample topics for English
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@english_subject_id, 'Alphabets and Phonics', NOW(), true, NOW()),
(@english_subject_id, 'Reading Comprehension', NOW(), true, NOW()),
(@english_subject_id, 'Grammar Basics', NOW(), true, NOW()),
(@english_subject_id, 'Creative Writing', NOW(), true, NOW());

-- Insert sample topics for Science
INSERT INTO ed_topics (subject_id, topic_name, publish_date, is_active, created_at)
VALUES 
(@science_subject_id, 'Plants and Animals', NOW(), true, NOW()),
(@science_subject_id, 'Weather and Seasons', NOW(), true, NOW()),
(@science_subject_id, 'Human Body Basics', NOW(), true, NOW());

-- Get customer IDs from users
SET @student1_customer_id = (SELECT customer_id FROM users WHERE user_id = 'student001');

-- Insert sample subject subscriptions (student001 has opted for Math and English)
INSERT INTO user_subject_subscriptions (customer_id, subject_id, subscribed_at, is_active)
VALUES 
(@student1_customer_id, @math_subject_id, NOW(), true),
(@student1_customer_id, @english_subject_id, NOW(), true);

-- Get topic IDs
SET @topic_addition = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Addition and Subtraction');
SET @topic_multiplication = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Multiplication Basics');
SET @topic_alphabets = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Alphabets and Phonics');

-- Insert sample topic subscriptions (student001 has opted for specific topics)
INSERT INTO user_topic_subscriptions (customer_id, topic_id, subscribed_at, is_active)
VALUES 
(@student1_customer_id, @topic_addition, NOW(), true),
(@student1_customer_id, @topic_multiplication, NOW(), true),
(@student1_customer_id, @topic_alphabets, NOW(), true);

-- Sample data summary:
-- Grade 1 has 5 subjects: Mathematics, English, Science, Social Studies, Art
-- student001 has opted for: Mathematics and English (2 opted, 3 unopted)
-- Mathematics has 4 topics, student001 has opted for 2 topics (2 opted, 2 unopted)
-- English has 4 topics, student001 has opted for 1 topic (1 opted, 3 unopted)
-- Science has 3 topics, student001 has opted for 0 topics (0 opted, 3 unopted)

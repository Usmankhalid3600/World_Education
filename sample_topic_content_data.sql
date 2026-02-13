-- Sample data for topic_contents table

-- Get topic IDs
SET @topic_addition = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Addition and Subtraction');
SET @topic_multiplication = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Multiplication Basics');
SET @topic_division = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Division Basics');
SET @topic_alphabets = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Alphabets and Phonics');
SET @topic_reading = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Reading Comprehension');
SET @topic_plants = (SELECT topic_id FROM ed_topics WHERE topic_name = 'Plants and Animals');

-- Get admin user ID for uploaded_by
SET @admin_customer_id = (SELECT customer_id FROM users WHERE user_id = 'admin001');

-- Insert topic contents for Mathematics topics

-- Addition and Subtraction (student001 has subscription)
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, uploaded_by, uploaded_at, is_active)
VALUES 
(@topic_addition, 'Addition Basics - Lesson 1.pdf', 'https://cdn.worldedu.com/math/addition/lesson1.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_addition, 'Addition Practice Worksheet.pdf', 'https://cdn.worldedu.com/math/addition/worksheet1.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_addition, 'Addition Video Tutorial.mp4', 'https://cdn.worldedu.com/math/addition/video1.mp4', 'VIDEO', @admin_customer_id, NOW(), true),
(@topic_addition, 'Interactive Addition Quiz', 'https://cdn.worldedu.com/math/addition/quiz1.html', 'INTERACTIVE', @admin_customer_id, NOW(), true);

-- Multiplication Basics (student001 has subscription)
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, uploaded_by, uploaded_at, is_active)
VALUES 
(@topic_multiplication, 'Multiplication Tables 1-10.pdf', 'https://cdn.worldedu.com/math/multiplication/tables.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_multiplication, 'Multiplication Tricks Video.mp4', 'https://cdn.worldedu.com/math/multiplication/video1.mp4', 'VIDEO', @admin_customer_id, NOW(), true),
(@topic_multiplication, 'Multiplication Games.html', 'https://cdn.worldedu.com/math/multiplication/games.html', 'INTERACTIVE', @admin_customer_id, NOW(), true);

-- Division Basics (student001 does NOT have subscription, but has Math subject subscription)
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, uploaded_by, uploaded_at, is_active)
VALUES 
(@topic_division, 'Division Introduction.pdf', 'https://cdn.worldedu.com/math/division/intro.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_division, 'Division Practice Problems.pdf', 'https://cdn.worldedu.com/math/division/practice.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_division, 'Division Explained - Video.mp4', 'https://cdn.worldedu.com/math/division/video1.mp4', 'VIDEO', @admin_customer_id, NOW(), true);

-- Insert topic contents for English topics

-- Alphabets and Phonics (student001 has subscription)
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, uploaded_by, uploaded_at, is_active)
VALUES 
(@topic_alphabets, 'ABC Song and Animation.mp4', 'https://cdn.worldedu.com/english/alphabets/abc-song.mp4', 'VIDEO', @admin_customer_id, NOW(), true),
(@topic_alphabets, 'Alphabet Tracing Worksheets.pdf', 'https://cdn.worldedu.com/english/alphabets/tracing.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_alphabets, 'Phonics Practice.pdf', 'https://cdn.worldedu.com/english/alphabets/phonics.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_alphabets, 'Interactive Alphabet Game.html', 'https://cdn.worldedu.com/english/alphabets/game.html', 'INTERACTIVE', @admin_customer_id, NOW(), true);

-- Reading Comprehension (student001 does NOT have subscription, but has English subject subscription)
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, uploaded_by, uploaded_at, is_active)
VALUES 
(@topic_reading, 'Simple Stories for Reading.pdf', 'https://cdn.worldedu.com/english/reading/stories.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_reading, 'Reading Comprehension Exercises.pdf', 'https://cdn.worldedu.com/english/reading/exercises.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_reading, 'Storytime Video.mp4', 'https://cdn.worldedu.com/english/reading/storytime.mp4', 'VIDEO', @admin_customer_id, NOW(), true);

-- Insert topic contents for Science topics

-- Plants and Animals (student001 does NOT have Science subject or topic subscription)
INSERT INTO topic_contents (topic_id, file_name, file_path_url, file_type, uploaded_by, uploaded_at, is_active)
VALUES 
(@topic_plants, 'Introduction to Plants.pdf', 'https://cdn.worldedu.com/science/plants/intro.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_plants, 'Animal Kingdom Overview.pdf', 'https://cdn.worldedu.com/science/plants/animals.pdf', 'PDF', @admin_customer_id, NOW(), true),
(@topic_plants, 'Plant Growth Time-lapse.mp4', 'https://cdn.worldedu.com/science/plants/timelapse.mp4', 'VIDEO', @admin_customer_id, NOW(), true),
(@topic_plants, 'Animal Habitats Game.html', 'https://cdn.worldedu.com/science/plants/game.html', 'INTERACTIVE', @admin_customer_id, NOW(), true);

-- Summary of test scenarios for student001:
-- 
-- 1. Addition and Subtraction (topicId from query):
--    - HAS topic subscription ✓
--    - Should see 4 contents
--    - Access type: TOPIC_SUBSCRIPTION
--
-- 2. Multiplication Basics:
--    - HAS topic subscription ✓
--    - Should see 3 contents
--    - Access type: TOPIC_SUBSCRIPTION
--
-- 3. Division Basics:
--    - NO topic subscription ✗
--    - BUT HAS Math subject subscription ✓
--    - Should see 3 contents
--    - Access type: SUBJECT_SUBSCRIPTION
--
-- 4. Alphabets and Phonics:
--    - HAS topic subscription ✓
--    - Should see 4 contents
--    - Access type: TOPIC_SUBSCRIPTION
--
-- 5. Reading Comprehension:
--    - NO topic subscription ✗
--    - BUT HAS English subject subscription ✓
--    - Should see 3 contents
--    - Access type: SUBJECT_SUBSCRIPTION
--
-- 6. Plants and Animals:
--    - NO topic subscription ✗
--    - NO Science subject subscription ✗
--    - Should see 0 contents, hasAccess: false
--    - HTTP 403 Forbidden

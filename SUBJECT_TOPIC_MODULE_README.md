# World Education - Subject & Topic Module Documentation

## 🔐 Security Architecture

The application now uses **JWT (JSON Web Token)** based authentication for secure API access.

### How JWT Security Works

1. **Login**: User logs in with credentials → Receives JWT token
2. **API Requests**: Include JWT token in Authorization header
3. **Validation**: Server validates token and authenticates user
4. **Access**: User can access protected endpoints

## 📡 API Endpoints

### Authentication

#### POST `/api/auth/login`

**Request:**
```json
{
  "userId": "student001",
  "password": "student123",
  "deviceId": "web-device-001",
  "deviceType": "WEB"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "customerId": 1,
    "userId": "student001",
    "userCategory": "STUDENT",
    "firstName": "Alice",
    "lastName": "Student",
    "email": "alice@example.com",
    "sessionId": 123,
    "loginTime": "2026-02-04T10:30:00",
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJjdXN0b21lcklkIjoxLCJ1c2VyQ2F0ZWdvcnkiOiJTVFVERU5UIiwic3ViIjoic3R1ZGVudDAwMSIsImlhdCI6MTczODY1MDAwMCwiZXhwIjoxNzM4NzM2NDAwfQ.abc123...",
    "message": "Login successful"
  },
  "timestamp": "2026-02-04T10:30:00"
}
```

**Important:** Save the `token` value - you'll need it for all subsequent API calls!

---

### Subjects Module

#### GET `/api/subjects/class/{classId}`

Get opted and unopted subjects for a specific class.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/subjects/class/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**Response:**
```json
{
  "success": true,
  "message": "Subjects retrieved successfully",
  "data": {
    "classId": 1,
    "className": "Grade 1",
    "optedSubjects": [
      {
        "subjectId": 1,
        "classId": 1,
        "subjectName": "Mathematics",
        "isActive": true,
        "isOpted": true
      },
      {
        "subjectId": 2,
        "classId": 1,
        "subjectName": "English",
        "isActive": true,
        "isOpted": true
      }
    ],
    "unoptedSubjects": [
      {
        "subjectId": 3,
        "classId": 1,
        "subjectName": "Science",
        "isActive": true,
        "isOpted": false
      },
      {
        "subjectId": 4,
        "classId": 1,
        "subjectName": "Social Studies",
        "isActive": true,
        "isOpted": false
      },
      {
        "subjectId": 5,
        "classId": 1,
        "subjectName": "Art",
        "isActive": true,
        "isOpted": false
      }
    ],
    "totalSubjects": 5,
    "optedCount": 2,
    "unoptedCount": 3
  },
  "timestamp": "2026-02-04T10:35:00"
}
```

**Features:**
- ✅ Returns two separate lists: `optedSubjects` and `unoptedSubjects`
- ✅ Shows which subjects the logged-in user has subscribed to
- ✅ Provides counts for easy UI display
- ✅ Only shows active subjects
- ✅ Requires authentication (JWT token)

---

### Topics Module

#### GET `/api/topics/subject/{subjectId}`

Get opted and unopted topics for a specific subject.

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/topics/subject/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**Response:**
```json
{
  "success": true,
  "message": "Topics retrieved successfully",
  "data": {
    "subjectId": 1,
    "subjectName": "Mathematics",
    "optedTopics": [
      {
        "topicId": 1,
        "subjectId": 1,
        "topicName": "Addition and Subtraction",
        "publishDate": "2026-02-04T10:00:00",
        "isActive": true,
        "isOpted": true
      },
      {
        "topicId": 2,
        "subjectId": 1,
        "topicName": "Multiplication Basics",
        "publishDate": "2026-02-04T10:00:00",
        "isActive": true,
        "isOpted": true
      }
    ],
    "unoptedTopics": [
      {
        "topicId": 3,
        "subjectId": 1,
        "topicName": "Division Basics",
        "publishDate": "2026-02-04T10:00:00",
        "isActive": true,
        "isOpted": false
      },
      {
        "topicId": 4,
        "subjectId": 1,
        "topicName": "Fractions Introduction",
        "publishDate": "2026-02-04T10:00:00",
        "isActive": true,
        "isOpted": false
      }
    ],
    "totalTopics": 4,
    "optedCount": 2,
    "unoptedCount": 2
  },
  "timestamp": "2026-02-04T10:36:00"
}
```

**Features:**
- ✅ Returns two separate lists: `optedTopics` and `unoptedTopics`
- ✅ Shows which topics the logged-in user has subscribed to
- ✅ Includes publish date for each topic
- ✅ Provides counts for easy UI display
- ✅ Only shows active topics
- ✅ Requires authentication (JWT token)

---

## 🏗️ Modular Architecture

### New Project Structure

```
src/main/java/com/worldedu/worldeducation/
│
├── auth/                          # Authentication Module
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   └── AuthService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── UserProfileRepository.java
│   │   └── UserSessionRepository.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── UserProfile.java
│   │   └── UserSession.java
│   └── dto/
│       ├── LoginRequest.java
│       └── LoginResponse.java
│
├── subject/                       # Subject Module
│   ├── controller/
│   │   └── SubjectController.java
│   ├── service/
│   │   └── SubjectService.java
│   ├── repository/
│   │   ├── EdClassRepository.java
│   │   ├── EdSubjectRepository.java
│   │   └── UserSubjectSubscriptionRepository.java
│   ├── entity/
│   │   ├── EdClass.java
│   │   ├── EdSubject.java
│   │   └── UserSubjectSubscription.java
│   └── dto/
│       ├── SubjectDTO.java
│       └── SubjectListResponse.java
│
├── topic/                         # Topic Module
│   ├── controller/
│   │   └── TopicController.java
│   ├── service/
│   │   └── TopicService.java
│   ├── repository/
│   │   ├── EdTopicRepository.java
│   │   └── UserTopicSubscriptionRepository.java
│   ├── entity/
│   │   ├── EdTopic.java
│   │   └── UserTopicSubscription.java
│   └── dto/
│       ├── TopicDTO.java
│       └── TopicListResponse.java
│
├── security/                      # Security Module
│   ├── config/
│   │   └── SecurityConfig.java
│   └── jwt/
│       ├── JwtUtil.java
│       └── JwtAuthenticationFilter.java
│
├── common/                        # Shared Components
│   ├── ApiResponse.java
│   └── Constants.java
│
├── enums/                         # Enumerations
│   ├── UserCategory.java
│   └── DeviceType.java
│
├── exception/                     # Exception Handling
│   ├── GlobalExceptionHandler.java
│   ├── AuthenticationException.java
│   ├── AccountLockedException.java
│   └── InvalidCredentialsException.java
│
└── util/                          # Utilities
    ├── PasswordUtil.java
    ├── DateTimeUtil.java
    └── ValidationUtil.java
```

**Benefits:**
- ✅ **Modular**: Each feature (auth, subject, topic) is in its own package
- ✅ **Scalable**: Easy to add new modules (payment, notification, etc.)
- ✅ **Maintainable**: Clear separation of concerns
- ✅ **Testable**: Each module can be tested independently

---

## 🔒 Security Features

### 1. JWT Token Authentication

**What is JWT?**
- Secure token-based authentication
- No need to store sessions on server
- Stateless authentication
- Industry standard for API security

**Token Contains:**
- User ID (userId)
- Customer ID (customerId)
- User Category (ADMIN/STUDENT)
- Expiration time (24 hours)

**Security Benefits:**
- ✅ Prevents unauthorized access
- ✅ Protects user data
- ✅ Automatic expiration
- ✅ Tamper-proof (cryptographically signed)

### 2. Spring Security Integration

**Features:**
- Automatic JWT validation on every request
- Role-based access control (ROLE_ADMIN, ROLE_STUDENT)
- Secure password encoding (BCrypt)
- Protection against common attacks (CSRF, etc.)

**How It Works:**
```
1. User → Login → Server validates credentials
2. Server → Generates JWT token → Returns to user
3. User → Stores token → Includes in every API request
4. Server → Validates token → Allows/Denies access
```

---

## 🚀 Quick Start Guide

### Step 1: Start the Application

```bash
./mvnw spring-boot:run
```

### Step 2: Insert Test Data

```bash
# Insert user data
mysql -u root -p world_education_db < sample_data.sql

# Insert subject/topic data
mysql -u root -p world_education_db < sample_subject_topic_data.sql
```

### Step 3: Login and Get Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "student001",
    "password": "student123",
    "deviceId": "web-001",
    "deviceType": "WEB"
  }'
```

**Copy the `token` from the response!**

### Step 4: Access Protected Endpoints

```bash
# Replace YOUR_TOKEN_HERE with the actual token from Step 3

# Get subjects for Grade 1
curl -X GET http://localhost:8080/api/subjects/class/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Get topics for Mathematics (subject_id = 1)
curl -X GET http://localhost:8080/api/topics/subject/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🧪 Testing Scenarios

### Test Data Overview

**Classes:**
- Grade 1 (class_id: 1) - 5 subjects
- Grade 2 (class_id: 2) - 5 subjects

**For student001:**
- **Opted Subjects**: Mathematics, English (2 subjects)
- **Unopted Subjects**: Science, Social Studies, Art (3 subjects)

**For Mathematics:**
- **Opted Topics**: Addition and Subtraction, Multiplication Basics (2 topics)
- **Unopted Topics**: Division Basics, Fractions Introduction (2 topics)

**For English:**
- **Opted Topics**: Alphabets and Phonics (1 topic)
- **Unopted Topics**: Reading Comprehension, Grammar Basics, Creative Writing (3 topics)

### Test Cases

1. **Login without token** → Should fail (401 Unauthorized)
2. **Login with invalid token** → Should fail (401 Unauthorized)
3. **Login with expired token** → Should fail (401 Unauthorized)
4. **Access subjects with valid token** → Should succeed
5. **Access topics with valid token** → Should succeed
6. **Verify opted vs unopted separation** → Should show correct lists

---

## 📊 Database Schema

### New Tables

**ed_classes**
```sql
class_id (PK)
class_name
class_number
is_active
created_at
```

**ed_subjects**
```sql
subject_id (PK)
class_id (FK → ed_classes)
subject_name
is_active
created_at
```

**user_subject_subscriptions**
```sql
subscription_id (PK)
customer_id (FK → users)
subject_id (FK → ed_subjects)
subscribed_at
is_active
```

**ed_topics**
```sql
topic_id (PK)
subject_id (FK → ed_subjects)
topic_name
publish_date
is_active
created_at
```

**user_topic_subscriptions**
```sql
subscription_id (PK)
customer_id (FK → users)
topic_id (FK → ed_topics)
subscribed_at
is_active
```

---

## 🔧 Configuration

### JWT Settings (application.properties)

```properties
# JWT Secret Key (512 bits minimum for HS512 algorithm)
jwt.secret=worldeducation-super-secret-key-for-jwt-token-generation-minimum-512-bits-required-for-hs512

# JWT Token Expiration (24 hours in milliseconds)
jwt.expiration=86400000
```

**⚠️ Production Note:** Change the secret key to a secure random value!

---

## 📝 API Response Format

All endpoints return data in this standardized format:

```json
{
  "success": boolean,
  "message": "string",
  "data": object | null,
  "timestamp": "datetime"
}
```

**Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-02-04T10:30:00"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2026-02-04T10:30:00"
}
```

---

## 🎯 Key Features Implemented

### Authentication
- ✅ JWT token generation on login
- ✅ Token includes user info (userId, customerId, userCategory)
- ✅ 24-hour token expiration
- ✅ Automatic token validation on API requests

### Subject Module
- ✅ Get all subjects for a class
- ✅ Separate opted and unopted subjects
- ✅ User-specific subscriptions
- ✅ Count of opted/unopted subjects

### Topic Module
- ✅ Get all topics for a subject
- ✅ Separate opted and unopted topics
- ✅ User-specific subscriptions
- ✅ Count of opted/unopted topics
- ✅ Publish date tracking

### Security
- ✅ JWT-based authentication
- ✅ Protected endpoints (require login)
- ✅ User verification on every request
- ✅ Role-based access (ADMIN/STUDENT)
- ✅ Automatic authentication from token

### Code Quality
- ✅ Modular architecture
- ✅ Separation of concerns
- ✅ Clean code organization
- ✅ Comprehensive logging
- ✅ Exception handling
- ✅ Input validation

---

## 🚀 Future Enhancements

1. **Subscription Management**
   - Add endpoint to subscribe to subjects
   - Add endpoint to subscribe to topics
   - Add endpoint to unsubscribe

2. **Advanced Features**
   - Get all opted subjects across all classes
   - Get all opted topics across all subjects
   - Subscription history
   - Recommendation engine

3. **Admin Features**
   - Manage classes, subjects, topics
   - View subscription analytics
   - Bulk operations

---

## 📞 Support

For issues or questions, check the logs or review the implementation code in the respective module packages.

**Module Locations:**
- Auth: `com.worldedu.worldeducation.auth.*`
- Subject: `com.worldedu.worldeducation.subject.*`
- Topic: `com.worldedu.worldeducation.topic.*`
- Security: `com.worldedu.worldeducation.security.*`

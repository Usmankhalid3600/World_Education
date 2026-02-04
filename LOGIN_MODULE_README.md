# World Education - Login Module Documentation

## Overview
The Login Module provides secure authentication with advanced security features including account locking, session management, and single-device login enforcement for students.

## Features

### 1. **Authentication**
- User ID and password-based authentication
- Secure password hashing (SHA-256)
- Failed login attempt tracking

### 2. **Account Security**
- Account locking after 5 failed login attempts
- Failed attempt counter with automatic reset on successful login
- Last login tracking

### 3. **Session Management**
- Multi-device support with device type tracking (WEB, ANDROID, IOS)
- Device ID tracking for unique device identification
- Session activity tracking

### 4. **Single Device Login Enforcement**
- **STUDENT users**: Only one active session allowed. New login automatically logs out existing session.
- **ADMIN users**: Multiple concurrent sessions allowed across different devices.

## API Endpoint

### POST `/api/auth/login`

Authenticates a user and creates a new session.

**Request Body:**
```json
{
  "userId": "student123",
  "password": "password123",
  "deviceId": "device-unique-id-12345",
  "deviceType": "WEB"
}
```

**Device Types:** `WEB`, `ANDROID`, `IOS`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "customerId": 1,
    "userId": "student123",
    "userCategory": "STUDENT",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "sessionId": 123,
    "loginTime": "2026-02-03T10:30:00",
    "message": "Login successful"
  },
  "timestamp": "2026-02-03T10:30:00"
}
```

**Error Responses:**

**401 Unauthorized - Invalid Credentials:**
```json
{
  "success": false,
  "message": "Invalid user ID or password",
  "data": null,
  "timestamp": "2026-02-03T10:30:00"
}
```

**403 Forbidden - Account Locked:**
```json
{
  "success": false,
  "message": "Account is locked due to multiple failed login attempts. Please contact support.",
  "data": null,
  "timestamp": "2026-02-03T10:30:00"
}
```

**400 Bad Request - Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "userId": "User ID is required",
    "password": "Password is required"
  },
  "timestamp": "2026-02-03T10:30:00"
}
```

## Architecture

### Package Structure
```
com.worldedu.worldeducation/
├── common/                  # Common utilities
│   ├── ApiResponse.java     # Standardized API response wrapper
│   └── Constants.java       # Application constants
├── controller/              # REST controllers
│   └── AuthController.java  # Authentication endpoints
├── dto/                     # Data Transfer Objects
│   ├── LoginRequest.java    # Login request payload
│   └── LoginResponse.java   # Login response payload
├── entity/                  # JPA entities
│   ├── User.java           # User entity
│   ├── UserProfile.java    # User profile entity
│   └── UserSession.java    # User session entity
├── enums/                   # Enumerations
│   ├── UserCategory.java   # ADMIN, STUDENT
│   └── DeviceType.java     # WEB, ANDROID, IOS
├── exception/               # Custom exceptions
│   ├── AuthenticationException.java
│   ├── AccountLockedException.java
│   ├── InvalidCredentialsException.java
│   └── GlobalExceptionHandler.java
├── repository/              # Data access layer
│   ├── UserRepository.java
│   ├── UserProfileRepository.java
│   └── UserSessionRepository.java
├── service/                 # Business logic
│   └── AuthService.java    # Authentication service
└── util/                    # Utility classes
    ├── PasswordUtil.java   # Password hashing and validation
    ├── DateTimeUtil.java   # Date/time utilities
    └── ValidationUtil.java # Validation utilities
```

### Design Principles
- **Separation of Concerns**: Controller handles HTTP, Service handles business logic
- **Single Responsibility**: Each class has a focused purpose
- **Reusability**: Common utilities for future features
- **Security**: Password hashing, account locking, session tracking
- **Validation**: Input validation at controller level
- **Exception Handling**: Centralized exception handling with appropriate HTTP status codes

## Business Logic Flow

### Login Process

1. **Validate Input**: Check required fields (userId, password, deviceId, deviceType)
2. **Find User**: Look up user by userId
3. **Check Account Status**: Verify account is not locked
4. **Update Login Attempt**: Record the login attempt timestamp
5. **Verify Password**: Compare hashed password
6. **Handle Failed Login** (if password incorrect):
   - Increment failed attempts counter
   - Lock account if attempts >= 5
   - Throw InvalidCredentialsException
7. **Handle Successful Login**:
   - Reset failed attempts to 0
   - Update last login timestamp
   - For STUDENT users: Deactivate all existing sessions
   - For ADMIN users: Allow multiple sessions
   - Create new session with device details
8. **Build Response**: Include user info, profile data, and session ID

## Database Tables Used

### `users`
- Stores authentication credentials
- Tracks failed login attempts
- Manages account lock status
- Records login timestamps

### `users_profile`
- Stores user personal information
- One-to-one relationship with users

### `user_sessions`
- Tracks active and inactive sessions
- Records device information
- Manages session activity

## Security Features

1. **Password Hashing**: All passwords are hashed using SHA-256 (Note: Consider BCrypt for production)
2. **Account Locking**: Automatic lock after 5 failed attempts
3. **Session Tracking**: Complete audit trail of user sessions
4. **Device Identification**: Track which devices users log in from
5. **Single Device Enforcement**: Students can only use one device at a time

## Configuration

### Database Configuration (application.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/world_education_db
spring.datasource.username=root
spring.datasource.password=root
```

### Security Configuration
- Max Failed Attempts: 5 (configurable in PasswordUtil)
- Password Hashing: SHA-256 (upgradeable to BCrypt)

## Future Enhancements

1. **JWT Token Support**: Add token-based authentication
2. **OAuth2 Integration**: Support social login
3. **Password Reset**: Email-based password recovery
4. **Two-Factor Authentication**: SMS/Email OTP
5. **Session Timeout**: Automatic session expiration
6. **IP Tracking**: Additional security layer
7. **Audit Logging**: Comprehensive activity logs
8. **Rate Limiting**: Prevent brute force attacks

## Testing

### Test Scenarios
1. Successful login with valid credentials
2. Failed login with invalid credentials
3. Account locking after 5 failed attempts
4. Single device login for STUDENT users
5. Multiple device login for ADMIN users
6. Session creation and tracking
7. Validation error handling

### Sample cURL Commands

**Successful Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "student123",
    "password": "password123",
    "deviceId": "web-device-001",
    "deviceType": "WEB"
  }'
```

## Dependencies

- Spring Boot 4.0.2
- Spring Web
- Spring Data JPA
- Spring Validation
- MySQL Connector
- Lombok
- Jakarta Persistence API

## Notes

- Update database credentials in `application.properties` before running
- Ensure MySQL server is running
- Database schema will be auto-created on first run (ddl-auto=update)
- Default server port: 8080

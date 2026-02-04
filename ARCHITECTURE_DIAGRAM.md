# Login Module - Architecture Diagram

## Request Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            CLIENT (Web/Android/iOS)                      │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     │ POST /api/auth/login
                                     │ {userId, password, deviceId, deviceType}
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CONTROLLER LAYER                                 │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │             AuthController.java                                   │  │
│  │  • Validates request (@Valid)                                     │  │
│  │  • Delegates to service layer                                     │  │
│  │  • Returns ApiResponse<LoginResponse>                             │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          SERVICE LAYER                                   │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │             AuthService.java                                      │  │
│  │                                                                    │  │
│  │  1. Find user by userId                                           │  │
│  │  2. Check if account is locked ──────► AccountLockedException    │  │
│  │  3. Update last login attempt                                     │  │
│  │  4. Verify password (using PasswordUtil)                          │  │
│  │     ├─ If wrong: Increment failed attempts                        │  │
│  │     │             Lock if attempts >= 5                            │  │
│  │     │             Throw InvalidCredentialsException                │  │
│  │     └─ If correct: Reset failed attempts                          │  │
│  │                    Update last login time                          │  │
│  │  5. Handle single device login (STUDENT only)                     │  │
│  │     └─ Deactivate all existing sessions                           │  │
│  │  6. Create new session                                            │  │
│  │  7. Build and return LoginResponse                                │  │
│  │                                                                    │  │
│  │  Uses: PasswordUtil, UserRepository, UserSessionRepository        │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        REPOSITORY LAYER                                  │
│  ┌───────────────────┐  ┌───────────────────┐  ┌──────────────────┐   │
│  │  UserRepository   │  │ UserProfileRepo   │  │ UserSessionRepo  │   │
│  │                   │  │                   │  │                  │   │
│  │ • findByUserId()  │  │ • findById()      │  │ • save()         │   │
│  │ • save()          │  │                   │  │ • deactivateAll..│   │
│  └───────────────────┘  └───────────────────┘  └──────────────────┘   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          DATABASE (MySQL)                                │
│  ┌───────────────────┐  ┌───────────────────┐  ┌──────────────────┐   │
│  │    users          │  │   users_profile   │  │  user_sessions   │   │
│  │ ───────────────── │  │ ───────────────── │  │ ──────────────── │   │
│  │ customer_id (PK)  │  │ customer_id (PK)  │  │ session_id (PK)  │   │
│  │ user_id           │  │ first_name        │  │ customer_id (FK) │   │
│  │ password_hash     │  │ last_name         │  │ device_id        │   │
│  │ failed_attempts   │  │ email             │  │ device_type      │   │
│  │ account_locked    │  │ mobile_no         │  │ login_time       │   │
│  │ last_login_at     │  │ country           │  │ last_activity_at │   │
│  │ user_category     │  │ ...               │  │ is_active        │   │
│  └───────────────────┘  └───────────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

## Single Device Login Flow (STUDENT)

```
User: student001
Device: Web Browser

Step 1: Login from WEB
┌─────────────────┐
│   WEB Browser   │──► POST /api/auth/login
└─────────────────┘    {userId: "student001", deviceType: "WEB"}
                            │
                            ▼
                    ┌───────────────────┐
                    │   AuthService     │
                    │ • Create session  │
                    └───────────────────┘
                            │
                            ▼
                    ┌───────────────────┐
                    │  user_sessions    │
                    │ session_id: 1     │
                    │ device_type: WEB  │
                    │ is_active: TRUE   │
                    └───────────────────┘

Step 2: Login from ANDROID (same user)
┌─────────────────┐
│  Android Phone  │──► POST /api/auth/login
└─────────────────┘    {userId: "student001", deviceType: "ANDROID"}
                            │
                            ▼
                    ┌───────────────────────────────┐
                    │      AuthService              │
                    │ • Deactivate existing session │
                    │ • Create new session          │
                    └───────────────────────────────┘
                            │
                            ▼
                    ┌───────────────────────────────┐
                    │      user_sessions            │
                    │ session_id: 1                 │
                    │ device_type: WEB              │
                    │ is_active: FALSE ◄───────────┐│  (Deactivated)
                    │                               ││
                    │ session_id: 2                 ││
                    │ device_type: ANDROID          ││
                    │ is_active: TRUE  ◄────────────┘│  (New Session)
                    └───────────────────────────────┘

Result: WEB session logged out, ANDROID session active
```

## Account Locking Flow

```
Attempt 1-4: Wrong Password
┌────────┐
│ Client │──► Login (wrong password)
└────────┘         │
                   ▼
           ┌───────────────┐
           │  AuthService  │──► Increment failed_login_attempts
           └───────────────┘    failed_attempts = 1, 2, 3, 4
                   │
                   ▼
           InvalidCredentialsException
           "Invalid user ID or password"

Attempt 5: Wrong Password
┌────────┐
│ Client │──► Login (wrong password)
└────────┘         │
                   ▼
           ┌───────────────────────────┐
           │     AuthService           │
           │ • failed_attempts = 5     │
           │ • Set account_locked=TRUE │
           └───────────────────────────┘
                   │
                   ▼
           AccountLockedException
           "Account locked due to multiple failed attempts"

Attempt 6+: Any Password
┌────────┐
│ Client │──► Login (any password)
└────────┘         │
                   ▼
           ┌───────────────────────────┐
           │     AuthService           │
           │ • Check account_locked    │
           │ • Account is locked!      │
           └───────────────────────────┘
                   │
                   ▼
           AccountLockedException (HTTP 403)
           "Account is locked. Contact support."
```

## Component Interaction Diagram

```
┌──────────────┐
│   Client     │
└──────┬───────┘
       │
       │ HTTP Request
       ▼
┌──────────────────────────────────────────────────────────────┐
│                   SPRING BOOT APPLICATION                     │
│                                                               │
│  ┌──────────────┐    ┌─────────────┐    ┌───────────────┐  │
│  │  Controller  │───▶│   Service   │───▶│  Repository   │  │
│  │              │◀───│             │◀───│               │  │
│  └──────────────┘    └─────┬───────┘    └───────┬───────┘  │
│         │                   │                    │           │
│         │                   │                    │           │
│         ▼                   ▼                    ▼           │
│  ┌──────────────┐    ┌─────────────┐    ┌───────────────┐  │
│  │     DTOs     │    │   Entities  │    │   Entities    │  │
│  │ • Request    │    │ • User      │    │   (JPA)       │  │
│  │ • Response   │    │ • Profile   │    │               │  │
│  └──────────────┘    │ • Session   │    └───────────────┘  │
│                      └─────────────┘                        │
│         │                   │                               │
│         │                   │ Uses                          │
│         │                   ▼                               │
│         │            ┌─────────────┐                        │
│         │            │  Utilities  │                        │
│         │            │ • Password  │                        │
│         │            │ • DateTime  │                        │
│         │            │ • Validation│                        │
│         │            └─────────────┘                        │
│         │                                                   │
│         │ On Error                                          │
│         ▼                                                   │
│  ┌──────────────────────────┐                              │
│  │  GlobalExceptionHandler  │                              │
│  │  • Catch exceptions      │                              │
│  │  • Return error response │                              │
│  └──────────────────────────┘                              │
│                                                             │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              │ JDBC
                              ▼
                      ┌───────────────┐
                      │  MySQL DB     │
                      │ • users       │
                      │ • profiles    │
                      │ • sessions    │
                      └───────────────┘
```

## Class Relationship Diagram

```
                    ┌──────────────┐
                    │     User     │
                    ├──────────────┤
                    │ customerId   │◀────────────┐
                    │ userId       │             │
                    │ passwordHash │             │ 1:1
                    │ userCategory │             │
                    └──────┬───────┘             │
                           │                     │
                           │ 1:N          ┌──────┴───────┐
                           │              │ UserProfile  │
                           │              ├──────────────┤
                           ▼              │ customerId   │
                    ┌──────────────┐     │ firstName    │
                    │ UserSession  │     │ lastName     │
                    ├──────────────┤     │ email        │
                    │ sessionId    │     └──────────────┘
                    │ customerId   │
                    │ deviceId     │
                    │ deviceType   │
                    │ isActive     │
                    └──────────────┘

Enums:
┌──────────────┐        ┌──────────────┐
│ UserCategory │        │  DeviceType  │
├──────────────┤        ├──────────────┤
│ • ADMIN      │        │ • WEB        │
│ • STUDENT    │        │ • ANDROID    │
└──────────────┘        │ • IOS        │
                        └──────────────┘
```

## Exception Hierarchy

```
java.lang.RuntimeException
         │
         └─► AuthenticationException
                     │
                     ├─► InvalidCredentialsException
                     │   • Wrong userId or password
                     │   • HTTP 401 Unauthorized
                     │
                     └─► AccountLockedException
                         • Account locked after 5 failed attempts
                         • HTTP 403 Forbidden
```

## Response Flow

```
Success Path:
┌────────┐   Request    ┌────────┐   Delegate   ┌─────────┐   Query   ┌──────┐
│ Client │─────────────▶│  Ctrl  │─────────────▶│ Service │──────────▶│  DB  │
└────────┘              └────────┘              └─────────┘           └──────┘
    ▲                                                 │                    │
    │                                                 ◀────────────────────┘
    │                                                 │
    │                                            Build Response
    │                                                 │
    │                                                 ▼
    │                                          ┌─────────────┐
    │                                          │ LoginResp   │
    │                                          │ + UserInfo  │
    │                                          │ + SessionId │
    │                                          └─────────────┘
    │                                                 │
    │   HTTP 200 OK                                   │
    │   ApiResponse<LoginResponse>                    │
    └─────────────────────────────────────────────────┘

Error Path:
┌────────┐   Request    ┌────────┐   Delegate   ┌─────────┐
│ Client │─────────────▶│  Ctrl  │─────────────▶│ Service │
└────────┘              └────────┘              └─────────┘
    ▲                                                 │
    │                                                 │ throws
    │                                                 ▼
    │                                      ┌───────────────────────┐
    │                                      │ InvalidCredentials    │
    │                                      │ AccountLocked         │
    │                                      │ ValidationError       │
    │                                      └──────────┬────────────┘
    │                                                 │
    │                                                 │ caught by
    │                                                 ▼
    │                                      ┌───────────────────────┐
    │                                      │ GlobalException       │
    │                                      │ Handler               │
    │                                      └──────────┬────────────┘
    │                                                 │
    │   HTTP 401/403/400/500                          │
    │   ApiResponse<error>                            │
    └─────────────────────────────────────────────────┘
```

# SignUp Module - Quick Reference

## 🚀 Quick Start

### 1. Database Setup
```bash
mysql -u root -p world_education_db < database_migration_signup.sql
```

### 2. Email Configuration
Edit `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
```

### 3. Test SignUp Flow

**Step 1: Send Verification Code**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "john_doe",
    "password": "Test@1234",
    "userCategory": "STUDENT",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "mobileNo": "+1234567890"
  }'
```

**Step 2: Check Email and Verify Code**
```bash
curl -X POST http://localhost:8080/api/auth/verify \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "john@example.com",
    "code": "123456"
  }'
```

### 4. Test Google OAuth
```bash
curl -X POST http://localhost:8080/api/auth/google \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "google@gmail.com",
    "firstName": "Google",
    "lastName": "User",
    "googleId": "1234567890"
  }'
```

---

## 📋 API Endpoints Summary

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/auth/signup` | POST | ❌ | Send verification code |
| `/api/auth/verify` | POST | ❌ | Verify code & create account |
| `/api/auth/google` | POST | ❌ | Google OAuth signup/signin |
| `/api/auth/login` | POST | ❌ | Password login |

---

## 📧 Email Setup (Gmail)

1. **Enable 2FA** in Google Account
2. **Generate App Password**:
   - Google Account → Security → App Passwords
   - App: Mail, Device: Other
   - Copy 16-character password
3. **Update application.properties**:
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=abcd efgh ijkl mnop
   ```

---

## 🗂️ Files Created/Updated

### New Files
1. **Entities:**
   - `CodeVerification.java` - Verification code storage
   - `SignUpMethod.java` - ENUM (DATA, GOOGLE)
   - `VerificationStatus.java` - ENUM (ACTIVE, USED, EXPIRED, LOGGED)

2. **DTOs:**
   - `SignUpRequest.java` - SignUp form data
   - `VerifyCodeRequest.java` - Code verification
   - `GoogleAuthRequest.java` - Google OAuth data
   - `SignUpResponse.java` - SignUp response

3. **Services:**
   - `SignUpService.java` - SignUp business logic
   - `EmailService.java` - Email sending

4. **Repositories:**
   - `CodeVerificationRepository.java` - Code data access

5. **Utilities:**
   - `PasswordUtil.java` - BCrypt hashing

6. **Documentation:**
   - `SIGNUP_MODULE_GUIDE.md` - Complete guide
   - `SIGNUP_QUICK_REFERENCE.md` - This file
   - `database_migration_signup.sql` - DB migration
   - `WorldEducation_SignUp_API.postman_collection.json` - Postman tests

### Updated Files
1. `User.java` - Added `signUpMethod` field
2. `UserRepository.java` - Added `findByCustomerId()`
3. `UserProfileRepository.java` - Added `findByEmail()`, `existsByEmail()`
4. `AuthController.java` - Added 3 new endpoints
5. `application.properties` - Added email & verification config
6. `pom.xml` - Added `spring-boot-starter-mail`

---

## 🔍 Database Tables

### users (updated)
```sql
signUp_method ENUM('DATA', 'GOOGLE') NOT NULL DEFAULT 'DATA'
```

### code_verification (new)
```sql
CREATE TABLE code_verification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    secret_code VARCHAR(10),
    action VARCHAR(50),
    expiry_time DATETIME,
    generation_time DATETIME,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'LOGGED'),
    user_id VARCHAR(255)
);
```

---

## ✅ Testing Checklist

- [ ] Run database migration script
- [ ] Configure Gmail SMTP credentials
- [ ] Test normal signup flow (2 steps)
- [ ] Verify email received with code
- [ ] Test Google OAuth signup (new user)
- [ ] Test Google OAuth signin (existing user)
- [ ] Test error: duplicate userId
- [ ] Test error: duplicate email
- [ ] Test error: invalid verification code
- [ ] Test error: expired verification code
- [ ] Test JWT token works after signup
- [ ] Import Postman collection
- [ ] Run all Postman tests

---

## 🎯 SignUp Flow Diagram

```
User                    Backend                  Email
  |                        |                        |
  |--1. POST /signup------>|                        |
  |    (user details)      |                        |
  |                        |--Generate code-------->|
  |                        |--Save to DB            |
  |                        |                        |
  |<------200 OK-----------|                        |
  |   "Code sent"          |                        |
  |                        |                        |
  |<--------------------------------Email with code-|
  |                        |                        |
  |--2. POST /verify------>|                        |
  |    (email + code)      |                        |
  |                        |--Validate code         |
  |                        |--Create user           |
  |                        |--Create profile        |
  |                        |--Mark code USED        |
  |                        |                        |
  |<------201 Created------|                        |
  |   JWT token            |                        |
  |                        |                        |
  |<--------------------------------Welcome email---|
```

---

## 🔧 Configuration Properties

```properties
# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Verification Code
app.verification.code.length=6
app.verification.code.validity-minutes=15

# Application
app.name=World Education
```

---

## 🐛 Troubleshooting

### Email not sending
**Check:** SMTP credentials, app password, Gmail 2FA enabled

### "Invalid or expired code"
**Fix:** Request new code (old codes auto-expire)

### "Email already registered"
**Fix:** Use password login or Google signin

### Google signin fails
**Check:** Email must be registered via Google, not password

---

## 📊 Sample Data Queries

### View users by signup method
```sql
SELECT user_id, signUp_method, email, created_at
FROM users u
JOIN users_profile p ON u.customer_id = p.customer_id;
```

### View active codes
```sql
SELECT user_id, secret_code, expiry_time
FROM code_verification
WHERE status = 'ACTIVE' AND expiry_time > NOW();
```

### Cleanup expired codes
```sql
UPDATE code_verification 
SET status = 'EXPIRED' 
WHERE status = 'ACTIVE' AND expiry_time < NOW();
```

---

## 📦 Production Deployment

### Before Going Live:
1. ✅ Update email credentials
2. ⚠️ Implement Google token validation
3. ⚠️ Add rate limiting
4. ⚠️ Use Redis for pending signups
5. ⚠️ Schedule cleanup job for expired codes
6. ⚠️ Add HTML email templates
7. ⚠️ Add reCAPTCHA
8. ⚠️ Set up monitoring

---

## 🎉 Success!

All features implemented and ready for testing:
- ✅ Two-step signup with email verification
- ✅ Google OAuth signup/signin
- ✅ Secure password hashing
- ✅ JWT token generation
- ✅ Email notifications
- ✅ Error handling
- ✅ Complete documentation

**Next Steps:** Import Postman collection and start testing! 🚀

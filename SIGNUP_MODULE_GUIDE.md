## SignUp & Google OAuth Module - Complete Guide

## Overview
This module implements a secure two-step signup process with email verification and Google OAuth integration for World Education platform.

### Features Implemented
✅ **Two-Step SignUp with Email Verification**
- Step 1: User submits details → System sends 6-digit code to email
- Step 2: User enters code → Account created → Auto login with JWT token

✅ **Google OAuth SignUp/SignIn**
- If email exists: Sign in (must be Google user)
- If email doesn't exist: Create account with `signUp_method=GOOGLE`
- No password required for Google users

✅ **Security Features**
- Email verification required for signup
- Verification codes expire after 15 minutes
- Previous codes invalidated when new code requested
- BCrypt password hashing
- Account locking after 5 failed login attempts
- JWT token for authenticated sessions

---

## 📋 Database Schema

### Updated `users` table
```sql
signUp_method ENUM('DATA', 'GOOGLE') NOT NULL DEFAULT 'DATA'
```
- `DATA`: User signed up with email/password
- `GOOGLE`: User signed up via Google OAuth

### New `code_verification` table
```sql
CREATE TABLE code_verification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    secret_code VARCHAR(10) NOT NULL,
    action VARCHAR(50) NOT NULL,           -- 'SIGNUP', 'PASSWORD_RESET', etc.
    expiry_time DATETIME NOT NULL,
    generation_time DATETIME NOT NULL,
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'LOGGED') NOT NULL,
    user_id VARCHAR(255) NOT NULL          -- Email or userId
);
```

---

## 🔌 API Endpoints

### 1. SignUp - Step 1: Initiate Signup
**Endpoint:** `POST /api/auth/signup`  
**Authentication:** None  
**Purpose:** Collect user details and send verification code to email

**Request Body:**
```json
{
  "userId": "john_doe",
  "password": "SecurePass123",
  "userCategory": "STUDENT",
  "firstName": "John",
  "middleName": "Michael",
  "lastName": "Doe",
  "email": "john@example.com",
  "mobileNo": "+1234567890",
  "country": "USA",
  "state": "California",
  "city": "Los Angeles",
  "address": "123 Main Street"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Verification code sent to your email",
  "data": {
    "message": "Verification code sent to your email",
    "email": "john@example.com",
    "codeValidityMinutes": 15
  }
}
```

**Errors:**
- `400 Bad Request`: User ID or email already exists
- `400 Bad Request`: Validation errors (missing required fields)
- `500 Internal Server Error`: Email sending failed

---

### 2. Verify Code - Step 2: Complete Signup
**Endpoint:** `POST /api/auth/verify`  
**Authentication:** None  
**Purpose:** Verify code and create user account

**Request Body:**
```json
{
  "email": "john@example.com",
  "code": "123456"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Signup successful! Welcome to World Education.",
  "data": {
    "message": "Signup successful! Welcome to World Education.",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": "john_doe",
    "customerId": 123,
    "userCategory": "STUDENT",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com"
  }
}
```

**What Happens:**
1. ✅ Verification code validated
2. ✅ User record created in `users` table
3. ✅ Profile created in `users_profile` table
4. ✅ Code marked as `USED`
5. ✅ Welcome email sent
6. ✅ JWT token generated for auto-login

**Errors:**
- `400 Bad Request`: Invalid or expired code
- `400 Bad Request`: Signup session expired

---

### 3. Google OAuth SignIn/SignUp
**Endpoint:** `POST /api/auth/google`  
**Authentication:** None  
**Purpose:** Handle Google OAuth authentication

**Request Body:**
```json
{
  "googleToken": "google_id_token_from_frontend",
  "email": "john@gmail.com",
  "firstName": "John",
  "lastName": "Doe",
  "googleId": "1234567890",
  "mobileNo": "+1234567890",
  "country": "USA",
  "state": "California",
  "city": "Los Angeles",
  "address": "123 Google Lane"
}
```

**Response (200 OK):**

**Case 1: Existing Google User (Sign In)**
```json
{
  "success": true,
  "message": "Google Sign-In successful!",
  "data": {
    "message": "Google Sign-In successful!",
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": "john",
    "customerId": 123,
    "userCategory": "STUDENT",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@gmail.com"
  }
}
```

**Case 2: New User (Sign Up)**
```json
{
  "success": true,
  "message": "Google Sign-Up successful! Welcome to World Education.",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": "john1",
    "customerId": 456,
    "userCategory": "STUDENT",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@gmail.com"
  }
}
```

**What Happens for New User:**
1. ✅ Check if email exists
2. ✅ Generate unique userId from email
3. ✅ Create user with `signUp_method=GOOGLE`
4. ✅ Set dummy password (not used for login)
5. ✅ Create profile
6. ✅ Send welcome email
7. ✅ Generate JWT token

**Errors:**
- `400 Bad Request`: Email registered with password (must use password login)
- `400 Bad Request`: Missing required fields

---

## 📧 Email Configuration

### Gmail SMTP Setup

**Step 1: Enable 2FA in Gmail**
1. Go to Google Account Settings
2. Security → 2-Step Verification → Enable

**Step 2: Generate App Password**
1. Security → App Passwords
2. Select App: "Mail"
3. Select Device: "Other (Custom name)" → "World Education"
4. Copy generated password (16 characters)

**Step 3: Update application.properties**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
```

### Email Templates

**Verification Code Email:**
```
Subject: World Education - Email Verification Code

Welcome to World Education!

Your verification code is: 123456

This code will expire in 15 minutes.

If you didn't request this code, please ignore this email.

Best regards,
World Education Team
```

**Welcome Email:**
```
Subject: Welcome to World Education!

Hello John,

Welcome to World Education! Your account has been created successfully.

Your User ID: john_doe

You can now log in and start exploring our educational content.

Best regards,
World Education Team
```

---

## 🧪 Testing Guide

### Test 1: Normal SignUp Flow

**Step 1: Initiate Signup**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "testuser001",
    "password": "Test@1234",
    "userCategory": "STUDENT",
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "mobileNo": "+1234567890",
    "country": "USA"
  }'
```

**Expected:** Email sent with 6-digit code

**Step 2: Check Email & Get Code**
- Open email inbox for `test@example.com`
- Copy verification code (e.g., `123456`)

**Step 3: Verify Code**
```bash
curl -X POST http://localhost:8080/api/auth/verify \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "test@example.com",
    "code": "123456"
  }'
```

**Expected:** Account created + JWT token returned

**Step 4: Use JWT Token**
```bash
curl -X GET http://localhost:8080/api/subjects/class/1 \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

---

### Test 2: Google OAuth SignUp

```bash
curl -X POST http://localhost:8080/api/auth/google \
  -H 'Content-Type: application/json' \
  -d '{
    "googleToken": "dummy_token",
    "email": "googleuser@gmail.com",
    "firstName": "Google",
    "lastName": "User",
    "googleId": "1234567890",
    "mobileNo": "+1234567890",
    "country": "USA"
  }'
```

**Expected:** Account created with `signUp_method=GOOGLE` + JWT token

---

### Test 3: Google OAuth SignIn (Existing User)

```bash
curl -X POST http://localhost:8080/api/auth/google \
  -H 'Content-Type: application/json' \
  -d '{
    "googleToken": "dummy_token",
    "email": "googleuser@gmail.com",
    "firstName": "Google",
    "lastName": "User",
    "googleId": "1234567890"
  }'
```

**Expected:** Sign in successful + JWT token (no new account created)

---

### Test 4: Error Cases

**Test 4a: Duplicate User ID**
```bash
# Try to signup with existing userId
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "student001",
    "email": "newemail@example.com",
    ...
  }'
```
**Expected:** `400 Bad Request` - "User ID already exists"

**Test 4b: Duplicate Email**
```bash
# Try to signup with existing email
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": "newuser",
    "email": "student001@example.com",
    ...
  }'
```
**Expected:** `400 Bad Request` - "Email already registered"

**Test 4c: Invalid Verification Code**
```bash
curl -X POST http://localhost:8080/api/auth/verify \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "test@example.com",
    "code": "999999"
  }'
```
**Expected:** `400 Bad Request` - "Invalid or expired verification code"

**Test 4d: Expired Verification Code**
- Wait 16 minutes after receiving code
- Try to verify with old code
**Expected:** `400 Bad Request` - "Invalid or expired verification code"

**Test 4e: Google Sign-In with Password Account**
```bash
# Try Google signin with email that used password signup
curl -X POST http://localhost:8080/api/auth/google \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "student001@example.com",
    ...
  }'
```
**Expected:** `400 Bad Request` - "This email is registered with password"

---

## 🔒 Security Considerations

### 1. **Verification Code Security**
- ✅ 6-digit random code (1 million combinations)
- ✅ 15-minute expiration
- ✅ One-time use (marked as USED after verification)
- ✅ Previous codes invalidated when new code requested

### 2. **Password Security**
- ✅ BCrypt hashing with salt
- ✅ Minimum 8 characters required
- ✅ Password expiry: 6 months (for DATA signups)
- ✅ No password storage for Google users

### 3. **Email Security**
- ✅ SMTP over TLS (port 587)
- ✅ App-specific password (not main Gmail password)
- ✅ Email validation before sending

### 4. **Google OAuth**
- ⚠️ **TODO:** Validate Google token with Google API
- Currently trusts frontend validation
- Production must verify `googleToken` server-side

### 5. **Rate Limiting**
- ⚠️ **TODO:** Implement rate limiting
- Prevent spam signup requests
- Limit verification code requests per email

---

## 📊 Database Queries

### View All Users by SignUp Method
```sql
SELECT 
    u.user_id,
    u.signUp_method,
    p.email,
    p.first_name,
    p.last_name,
    u.created_at
FROM users u
JOIN users_profile p ON u.customer_id = p.customer_id
ORDER BY u.created_at DESC;
```

### View Active Verification Codes
```sql
SELECT 
    user_id as email,
    secret_code,
    status,
    generation_time,
    expiry_time,
    TIMESTAMPDIFF(MINUTE, NOW(), expiry_time) as minutes_until_expiry
FROM code_verification
WHERE status = 'ACTIVE'
AND expiry_time > NOW()
ORDER BY generation_time DESC;
```

### Cleanup Expired Codes (Run Daily)
```sql
UPDATE code_verification 
SET status = 'EXPIRED' 
WHERE status = 'ACTIVE' 
AND expiry_time < NOW();
```

---

## 📱 Frontend Integration

### React Example: SignUp Flow

```javascript
// Step 1: Submit Signup Form
async function handleSignUp(formData) {
  const response = await fetch('http://localhost:8080/api/auth/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(formData)
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Show verification code input
    showVerificationCodeInput(formData.email);
  } else {
    showError(result.message);
  }
}

// Step 2: Verify Code
async function handleVerifyCode(email, code) {
  const response = await fetch('http://localhost:8080/api/auth/verify', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, code })
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Save JWT token
    localStorage.setItem('token', result.data.token);
    localStorage.setItem('user', JSON.stringify(result.data));
    
    // Redirect to dashboard
    window.location.href = '/dashboard';
  } else {
    showError(result.message);
  }
}
```

### React Example: Google OAuth

```javascript
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';

function GoogleSignInButton() {
  const handleGoogleSuccess = async (credentialResponse) => {
    const googleToken = credentialResponse.credential;
    
    // Decode JWT to get user info
    const decoded = JSON.parse(atob(googleToken.split('.')[1]));
    
    const response = await fetch('http://localhost:8080/api/auth/google', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        googleToken: googleToken,
        email: decoded.email,
        firstName: decoded.given_name,
        lastName: decoded.family_name,
        googleId: decoded.sub
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      localStorage.setItem('token', result.data.token);
      window.location.href = '/dashboard';
    }
  };
  
  return (
    <GoogleOAuthProvider clientId="YOUR_GOOGLE_CLIENT_ID">
      <GoogleLogin
        onSuccess={handleGoogleSuccess}
        onError={() => console.log('Login Failed')}
      />
    </GoogleOAuthProvider>
  );
}
```

---

## 🚀 Deployment Checklist

### Before Production:
1. ✅ Update email credentials in application.properties
2. ⚠️ Implement Google token validation
3. ⚠️ Add rate limiting
4. ⚠️ Set up Redis for pending signups (instead of in-memory Map)
5. ⚠️ Configure scheduled job to clean expired codes
6. ✅ Update CORS settings
7. ⚠️ Add logging and monitoring
8. ⚠️ Set up email templates with HTML
9. ⚠️ Add reCAPTCHA for signup forms
10. ✅ Test email deliverability

---

## 📞 Support & Troubleshooting

### Issue: Emails not sending
**Solution:** Check SMTP credentials and Gmail app password

### Issue: Verification code expired
**Solution:** Request new code (previous codes auto-invalidated)

### Issue: "Email already registered"
**Solution:** Use password login or password reset flow

### Issue: Google sign-in fails for password user
**Solution:** User must use password login for accounts created with password

---

## Summary

✅ Complete two-step signup with email verification  
✅ Google OAuth integration for signup and signin  
✅ Secure password hashing with BCrypt  
✅ JWT token for authenticated sessions  
✅ Welcome emails after successful signup  
✅ Code expiration and one-time use  
✅ Proper error handling and validation  

All endpoints tested and ready for integration! 🎉

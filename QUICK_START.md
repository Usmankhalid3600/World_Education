# Quick Start Guide - Login Module

## Prerequisites
- Java 21+
- MySQL 8.0+
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Setup Steps

### 1. Database Setup
```bash
# Start MySQL server
# Create database (will be auto-created if using provided configuration)
mysql -u root -p
CREATE DATABASE world_education_db;
```

### 2. Configure Database Connection
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/world_education_db?createDatabaseIfNotExist=true
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 3. Build the Project
```bash
# Navigate to project directory
cd /Users/ukhalid/Documents/Shan_Project/WorldEducation

# Clean and build
./mvnw clean install

# Or if on Windows
mvnw.cmd clean install
```

### 4. Run the Application
```bash
# Run using Maven
./mvnw spring-boot:run

# Or run the JAR file
java -jar target/WorldEducation-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### 5. Insert Sample Data
After the application starts and creates the tables, run the sample data script:
```bash
mysql -u root -p world_education_db < sample_data.sql
```

### 6. Test the API

#### Using cURL:
```bash
# Login as STUDENT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "student001",
    "password": "student123",
    "deviceId": "web-device-001",
    "deviceType": "WEB"
  }'

# Login as ADMIN
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "admin001",
    "password": "admin123",
    "deviceId": "web-device-002",
    "deviceType": "WEB"
  }'
```

#### Using Postman:
1. Import `WorldEducation_Login_API.postman_collection.json`
2. Run the requests from the collection

## Test Credentials

### Admin User
- **User ID:** admin001
- **Password:** admin123
- **Features:** Can login from multiple devices simultaneously

### Student Users
- **User ID:** student001
- **Password:** student123
- **Features:** Single device login (new login logs out from previous device)

- **User ID:** student002
- **Password:** test123
- **Features:** Single device login

## Testing Scenarios

### 1. Successful Login
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

Expected: 200 OK with user details and session ID

### 2. Single Device Login (STUDENT)
```bash
# First login from web
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "student001",
    "password": "student123",
    "deviceId": "web-001",
    "deviceType": "WEB"
  }'

# Second login from Android (will deactivate web session)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "student001",
    "password": "student123",
    "deviceId": "android-001",
    "deviceType": "ANDROID"
  }'
```

Expected: Android session active, web session deactivated in database

### 3. Multiple Device Login (ADMIN)
```bash
# Login from web
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "admin001",
    "password": "admin123",
    "deviceId": "web-001",
    "deviceType": "WEB"
  }'

# Login from mobile (both sessions remain active)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "admin001",
    "password": "admin123",
    "deviceId": "mobile-001",
    "deviceType": "ANDROID"
  }'
```

Expected: Both sessions remain active

### 4. Account Locking (5 Failed Attempts)
```bash
# Attempt 1-5 with wrong password
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "userId": "student002",
      "password": "wrongpassword",
      "deviceId": "web-001",
      "deviceType": "WEB"
    }'
done
```

Expected: Account locked after 5th attempt

### 5. Validation Error
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "student001"
  }'
```

Expected: 400 Bad Request with validation errors

## Verify Database Changes

### Check active sessions:
```sql
SELECT * FROM user_sessions WHERE is_active = true;
```

### Check failed login attempts:
```sql
SELECT user_id, failed_login_attempts, account_locked 
FROM users;
```

### Check login history:
```sql
SELECT u.user_id, u.last_login_at, u.failed_login_attempts
FROM users u;
```

## Troubleshooting

### Issue: Application won't start
**Solution:** Check if MySQL is running and credentials are correct

### Issue: Tables not created
**Solution:** Verify `spring.jpa.hibernate.ddl-auto=update` in application.properties

### Issue: Connection refused
**Solution:** Ensure MySQL port 3306 is open and accessible

### Issue: Sample data fails to insert
**Solution:** Ensure tables exist first (run application once to create tables)

## Logs

Application logs are printed to console. Check for:
- Login attempts
- Session creation
- Account locking events
- SQL queries (when debug enabled)

## Next Steps

After successful setup:
1. Test all API endpoints
2. Verify session management
3. Check account locking behavior
4. Review database entries
5. Implement additional features (JWT tokens, password reset, etc.)

## Support

For issues or questions:
- Check logs in console
- Verify database connectivity
- Review application.properties configuration
- Check MySQL error logs

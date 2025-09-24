# HanoiGo Backend Setup Notes

## Database Setup
- Database: `hanoigo_db`
- User: `hanoigo_user`
- Password: `1`
- Port: 5432

## Application
- Port: 9090
- Main class: `HanoiGoApplication`

## Run Commands
```bash
cd HanoiGo/backend
./mvnw spring-boot:run
```

## API Endpoints
- Health: `GET http://localhost:9090/api/health/`
- Database: `GET http://localhost:9090/api/health/db`
- Register: `POST http://localhost:9090/api/auth/register`
- Login: `POST http://localhost:9090/api/auth/login`
- Forgot Password: `POST http://localhost:9090/api/auth/forgot-password`
- Reset Password: `POST http://localhost:9090/api/auth/reset-password`

## Features Implemented
✅ User Registration với email unique validation
✅ Password validation (8+ chars, letter, number, special char)
✅ User Login với JWT token
✅ Forgot Password với OTP qua email
✅ Reset Password với OTP verification
✅ Email service (Gmail SMTP)
✅ PostgreSQL integration
✅ Spring Security configuration
✅ Global exception handling

## Email Configuration
- Gmail: thenamdivine@gmail.com
- App Password: pfhz tjwq vgvr mmea

## Database Schema
```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- OTP tokens table
CREATE TABLE otp_tokens (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Technologies Used
- Spring Boot 3.5.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT Authentication
- BCrypt Password Encoding
- JavaMailSender
- Lombok

## Next Steps
- Add role-based authorization
- Implement email verification for registration
- Add rate limiting for OTP requests
- Implement refresh token mechanism
- Add API documentation with Swagger
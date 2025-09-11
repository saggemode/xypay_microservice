# Xypay User Registration API Documentation

This document provides comprehensive information about all user registration endpoints available in the Xypay application.

## Base URL
All endpoints are prefixed with: `/api/auth`

## Endpoints Overview

### 1. User Registration
**Endpoint:** `POST /api/auth/register`

**Description:** Register a new user in the system with username, email, password, and phone number.

**Request Body:**
```json
{
  "username": "austin",
  "email": "lidov27389@percyfx.com",
  "password": "Readings123",
  "phone": "+2347038655955"
}
```

**Response:**
- **Success (200 OK):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "userId": 123,
  "accountNumber": "7038655955"
}
```

- **Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Username already exists"
}
```

**Notes:**
- Username must be unique
- Email must be valid and unique
- Phone number should include country code
- Password requirements are enforced by the service

---

### 2. Alternative User Registration
**Endpoint:** `POST /api/auth/register-alt`

**Description:** Alternative registration endpoint that accepts `password1` instead of `password` field.

**Request Body:**
```json
{
  "username": "austin",
  "email": "lidov27389@percyfx.com",
  "password1": "Readings123",
  "phone": "+2347038655955"
}
```

**Response:** Same as standard registration endpoint

**Notes:**
- This endpoint is provided for compatibility with different client implementations
- Functionally identical to `/register` but accepts `password1` field

---

### 3. Phone Number Verification
**Endpoint:** `POST /api/auth/verify-phone`

**Description:** Verify a phone number using OTP (One-Time Password) sent via SMS.

**Request Body:**
```json
{
  "phone": "+2347038655955",
  "otp": "123456"
}
```

**Response:**
- **Success (200 OK):**
```json
{
  "success": true,
  "message": "Phone number verified successfully"
}
```

- **Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Invalid OTP or phone number"
}
```

**Notes:**
- OTP is typically 6 digits
- OTP has an expiration time (usually 5-10 minutes)
- Phone number must match the one used during registration

---

### 4. Resend OTP
**Endpoint:** `POST /api/auth/resend-otp`

**Description:** Resend OTP to the specified phone number.

**Request Body:**
```json
{
  "phone": "+2347038655955"
}
```

**Response:**
- **Success (200 OK):**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

- **Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Phone number not found or invalid"
}
```

**Notes:**
- Can only resend OTP for registered phone numbers
- Rate limiting may apply to prevent abuse
- Previous OTP becomes invalid when new one is sent

---

### 5. Get User by Account Number
**Endpoint:** `GET /api/auth/user/{accountNumber}`

**Description:** Retrieve user information using their account number.

**Path Parameters:**
- `accountNumber` (string): The user's account number

**Example:** `GET /api/auth/user/7038655955`

**Response:**
- **Success (200 OK):**
```json
{
  "success": true,
  "user": {
    "id": 123,
    "username": "austin",
    "email": "lidov27389@percyfx.com",
    "phone": "+2347038655955",
    "accountNumber": "7038655955",
    "isEmailVerified": true,
    "isPhoneVerified": true
  }
}
```

- **Error (404 Not Found):**
```json
{
  "success": false,
  "message": "User not found"
}
```

---

### 6. Email Verification
**Endpoint:** `GET /api/auth/verify-email`

**Description:** Verify user's email address using verification token sent via email.

**Query Parameters:**
- `uid` (string): User profile ID
- `token` (string): Email verification token

**Example:** `GET /api/auth/verify-email?uid=123&token=abc123def456`

**Response:**
- **Success (200 OK):**
```json
{
  "success": true,
  "message": "Email verified successfully"
}
```

- **Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Invalid or expired verification token"
}
```

**Notes:**
- Verification link is typically sent to user's email during registration
- Token has an expiration time
- Once verified, the token becomes invalid

---

### 7. Resend Email Verification
**Endpoint:** `POST /api/auth/resend-verification-email`

**Description:** Resend email verification link to the user's email address.

**Request Body:**
```json
{
  "email": "lidov27389@percyfx.com"
}
```

**Response:**
- **Success (200 OK):**
```json
{
  "success": true,
  "message": "Verification email sent successfully"
}
```

- **Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "User not found"
}
```

**Notes:**
- Email must be registered in the system
- New verification token is generated, invalidating the previous one
- Rate limiting may apply

---

## Registration Flow

### Typical User Registration Process:

1. **Initial Registration**
   - User calls `POST /api/auth/register` with their details
   - System creates user account and sends OTP to phone
   - System sends email verification link to email address

2. **Phone Verification**
   - User receives OTP via SMS
   - User calls `POST /api/auth/verify-phone` with OTP
   - If OTP is incorrect, user can call `POST /api/auth/resend-otp`

3. **Email Verification**
   - User clicks verification link in email (calls `GET /api/auth/verify-email`)
   - If link is expired, user can request new one via `POST /api/auth/resend-verification-email`

4. **Account Ready**
   - Once both phone and email are verified, user account is fully activated
   - User can retrieve their information using `GET /api/auth/user/{accountNumber}`

## Error Handling

All endpoints return consistent error responses with:
- `success`: boolean indicating operation result
- `message`: descriptive error message
- Appropriate HTTP status codes (200, 400, 404, 500)

## Security Considerations

- All sensitive operations require proper validation
- OTP and email tokens have expiration times
- Rate limiting is implemented to prevent abuse
- Phone numbers and emails must be unique per user
- Passwords are securely hashed before storage

## Content Types

- **Request Content-Type:** `application/json`
- **Response Content-Type:** `application/json`

## Authentication

These registration endpoints are public and do not require authentication. However, once registered and verified, users will need to authenticate for other API operations.

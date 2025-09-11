# Complete Testing Guide - Django-Compatible XyPay System

This guide shows how to test the complete user registration and KYC system that perfectly matches your Django implementation.

## 🚀 Quick Start Testing

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. Test Complete Registration Flow (Your Exact Example)

#### Step 1: Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "austin",
    "email": "lidov27389@percyfx.com",
    "password": "Readings123",
    "phone": "+2347038655955"
  }'
```

**Expected Response:**
```json
{
    "success": true,
    "message": "Registration successful. Please verify your phone number with the OTP sent.",
    "user_id": 1,
    "account_number": "7038655955",
    "phone": "+2347038655955",
    "otp_sent": true
}
```

#### Step 2: Check Application Logs for OTP
Look for log entry like:
```
INFO  - Sending OTP 123456 to phone number: +2347038655955
```

#### Step 3: Verify Phone Number
```bash
curl -X POST http://localhost:8080/api/auth/verify-phone \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+2347038655955",
    "otp": "123456"
  }'
```

**Expected Response:**
```json
{
    "success": true,
    "message": "Phone number verified successfully",
    "account_number": "7038655955",
    "kyc_level": "TIER_1"
}
```

#### Step 4: Get User Profile (Django-Compatible)
```bash
curl -X GET http://localhost:8080/api/auth/profile/account/7038655955
```

**Expected Response (Matches Django UserProfileSerializer):**
```json
{
    "id": "uuid-string",
    "username": "austin",
    "email": "lidov27389@percyfx.com",
    "phone": "+2347038655955",
    "account_number": "7038655955",
    "is_verified": true,
    "enabled": true,
    "created_at": "2024-01-01T10:00:00",
    "updated_at": "2024-01-01T10:00:00",
    "kyc": null,
    "wallet": {
        "id": 1,
        "account_number": "7038655955",
        "alternative_account_number": "7038655955",
        "balance": "0.00",

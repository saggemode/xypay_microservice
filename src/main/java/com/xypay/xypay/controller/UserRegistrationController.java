package com.xypay.xypay.controller;

import com.xypay.xypay.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserRegistrationController {
    
    @Autowired
    private UserRegistrationService userRegistrationService;
    
    /**
     * Register a new user
     * Example request body:
     * {
     *   "username": "austin",
     *   "email": "lidov27389@percyfx.com",
     *   "password": "Readings123",
     *   "phone": "+2347038655955"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String phone = request.get("phone");
        
        Map<String, Object> response = userRegistrationService.registerUser(username, email, password, phone);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verify phone number with OTP (legacy method - requires phone + otp)
     * Example request body:
     * {
     *   "phone": "+2347038655955",
     *   "otp": "123456"
     * }
     */
    @PostMapping("/verify-phone")
    public ResponseEntity<Map<String, Object>> verifyPhone(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String otp = request.get("otp");
        
        Map<String, Object> response = userRegistrationService.verifyPhoneNumber(phone, otp);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verify phone number with verification token (improved method - only requires token + otp)
     * Example request body:
     * {
     *   "verification_token": "abc123-def456-789",
     *   "otp": "123456"
     * }
     */
    @PostMapping("/verify-phone-token")
    public ResponseEntity<Map<String, Object>> verifyPhoneWithToken(@RequestBody Map<String, String> request) {
        String verificationToken = request.get("verification_token");
        String otp = request.get("otp");
        
        Map<String, Object> response = userRegistrationService.verifyPhoneWithToken(verificationToken, otp);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Resend OTP to phone number
     * Example request body:
     * {
     *   "phone": "+2347038655955"
     * }
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        
        Map<String, Object> response = userRegistrationService.resendOtp(phone);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get user information by account number
     * Example: GET /api/auth/user/7038655955
     */
    @GetMapping("/user/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getUserByAccountNumber(@PathVariable String accountNumber) {
        Map<String, Object> response = userRegistrationService.getUserByAccountNumber(accountNumber);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get user information by phone number (fintech-style lookup)
     * Example: GET /api/auth/user/phone/+2347038655955
     * Example: GET /api/auth/user/phone/07038655955
     */
    @GetMapping("/user/phone/{phoneNumber}")
    public ResponseEntity<Map<String, Object>> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        Map<String, Object> response = userRegistrationService.getUserByPhoneNumber(phoneNumber);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * JWT-based OTP verification (only requires OTP code - user identified from JWT token)
     * Example request body:
     * {
     *   "otp": "130087"
     * }
     * Requires: Authorization: Bearer <jwt_token>
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtpWithJwt(@RequestBody Map<String, String> request, 
                                                                @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract JWT token from Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Authorization header with Bearer token is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            String otp = request.get("otp");
            
            if (otp == null || otp.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "OTP is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Extract username from JWT token (this will be handled by the service)
            // For now, we'll pass the token to the service to extract the username
            Map<String, Object> response = userRegistrationService.verifyOtpWithJwtToken(token, otp);
            
            if ((Boolean) response.get("success")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Verification failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Alternative registration endpoint that matches your Python example format
     * Example request body:
     * {
     *   "username": "austin",
     *   "email": "lidov27389@percyfx.com",
     *   "password1": "Readings123",
     *   "phone": "+2347038655955"
     * }
     */
    @PostMapping("/register-alt")
    public ResponseEntity<Map<String, Object>> registerUserAlt(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password1"); // Different key in this format
        String phone = request.get("phone");
        
        Map<String, Object> response = userRegistrationService.registerUser(username, email, password, phone);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
}
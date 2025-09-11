package com.xypay.xypay.controller;

import com.xypay.xypay.security.JwtUtil;
import com.xypay.xypay.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * JWT Login endpoint
     * Example request body:
     * {
     *   "username": "austin",
     *   "password": "Readings123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Generate JWT tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("token_type", "Bearer");
            response.put("username", username);
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * JWT Token refresh endpoint
     * Example request body:
     * {
     *   "refresh_token": "eyJhbGciOiJIUzI1NiJ9..."
     * }
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String refreshToken = request.get("refresh_token");
            
            if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
                response.put("success", false);
                response.put("message", "Invalid refresh token");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                response.put("success", false);
                response.put("message", "Token is not a refresh token");
                return ResponseEntity.badRequest().body(response);
            }
            
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(userDetails);
            
            response.put("success", true);
            response.put("message", "Token refreshed successfully");
            response.put("access_token", newAccessToken);
            response.put("token_type", "Bearer");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token refresh failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * JWT Token validation endpoint
     * Example request body:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9..."
     * }
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = request.get("token");
            
            if (token == null) {
                response.put("success", false);
                response.put("message", "Token is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                response.put("success", true);
                response.put("message", "Token is valid");
                response.put("username", username);
            } else {
                response.put("success", false);
                response.put("message", "Token is invalid or expired");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token validation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

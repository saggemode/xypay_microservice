package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.XySaveAutoSaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/xysave/auto-save")
public class XySaveAutoSaveController {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveAutoSaveController.class);
    
    @Autowired
    private XySaveAutoSaveService xySaveAutoSaveService;
    
    /**
     * Enable auto-save
     */
    @PostMapping("/enable")
    public ResponseEntity<Map<String, Object>> enableAutoSave(@Valid @RequestBody XySaveAutoSaveRequestDTO request, 
                                                              Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            if (Boolean.TRUE.equals(request.getEnabled())) {
                var account = xySaveAutoSaveService.enableAutoSave(
                    user, request.getPercentage(), request.getMinAmount());
                
                Map<String, Object> response = Map.of(
                    "message", String.format("Auto-save enabled at %s%% with minimum amount ₦%s", 
                        request.getPercentage(), request.getMinAmount()),
                    "account", new XySaveAccountDTO(account)
                );
                
                return ResponseEntity.ok(response);
            } else {
                var account = xySaveAutoSaveService.disableAutoSave(user);
                
                Map<String, Object> response = Map.of(
                    "message", "Auto-save disabled",
                    "account", new XySaveAccountDTO(account)
                );
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Error configuring auto-save: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to configure auto-save"));
        }
    }
    
    /**
     * Get auto-save status
     */
    @GetMapping("/status")
    public ResponseEntity<XySaveAutoSaveService.AutoSaveStatus> getAutoSaveStatus(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveAutoSaveService.AutoSaveStatus status = xySaveAutoSaveService.getAutoSaveStatus(user);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting auto-save status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

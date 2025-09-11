package com.xypay.xypay.controller;

import com.xypay.xypay.service.SetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EnhancedSetupController {
    
    @Autowired
    private SetupService setupService;
    
    @GetMapping("/setup")
    public String setupPage(Model model) {
        if (setupService.isSystemInitialized()) {
            return "redirect:/login";
        }
        return "setup";
    }
    
    @PostMapping("/setup")
    public String initializeSystem(
            @RequestParam String bankName,
            @RequestParam String bankCode,
            @RequestParam String adminUsername,
            @RequestParam String adminPassword,
            @RequestParam String adminConfirmPassword,
            @RequestParam String adminFirstName,
            @RequestParam String adminLastName,
            @RequestParam String adminEmail,
            Model model) {
        
        if (setupService.isSystemInitialized()) {
            return "redirect:/login";
        }
        
        if (!adminPassword.equals(adminConfirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "setup";
        }
        
        if (adminPassword.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long");
            return "setup";
        }
        
        if (bankName == null || bankName.isEmpty()) {
            model.addAttribute("error", "Bank name is required");
            return "setup";
        }
        
        if (bankCode == null || bankCode.isEmpty()) {
            model.addAttribute("error", "Bank code is required");
            return "setup";
        }
        
        if (!bankCode.matches("[A-Z0-9]{3,6}")) {
            model.addAttribute("error", "Bank code must be 3-6 uppercase letters or numbers");
            return "setup";
        }
        
        SetupService.AdminUser adminUser = new SetupService.AdminUser(
            adminUsername, adminPassword, adminFirstName, adminLastName, adminEmail);
        
        try {
            boolean success = setupService.initializeSystem(bankName, bankCode, adminUser);
            
            if (success) {
                model.addAttribute("success", "System initialized successfully! You can now login with your administrator account.");
                return "redirect:/login";
            } else {
                model.addAttribute("error", "Failed to initialize system. Please check your inputs and try again.");
                return "setup";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid input: " + e.getMessage());
            return "setup";
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            return "setup";
        }
    }
}
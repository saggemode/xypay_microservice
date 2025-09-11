package com.xypay.xypay.controller;

import com.xypay.xypay.service.SetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    
    @Autowired
    private SetupService setupService;
    
    @GetMapping("/")
    public String home() {
        // Check if system is initialized, if not redirect to setup
        if (!setupService.isSystemInitialized()) {
            return "redirect:/setup";
        }
        return "redirect:/user/dashboard";
    }
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String register(@RequestParam(value = "error", required = false) String error,
                          Model model) {
        if (error != null) {
            model.addAttribute("error", "Registration failed. Please try again.");
        }
        return "auth/register";
    }
    
    @GetMapping("/user/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        // Add dashboard data here
        model.addAttribute("accountBalance", 150000.00);
        model.addAttribute("transactionCount", 12);
        model.addAttribute("pendingPayments", 3);
        return "dashboard";
    }
}
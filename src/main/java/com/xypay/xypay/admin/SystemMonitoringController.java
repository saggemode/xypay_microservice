package com.xypay.xypay.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.AccountRepository;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.repository.AuditLogRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class SystemMonitoringController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @GetMapping("/monitoring")
    public String systemMonitoring(Model model) {
        // System Statistics
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();
        long totalAuditLogs = auditLogRepository.count();
        
        // Generate mock real-time data
        List<SystemMetric> metrics = generateSystemMetrics();
        List<PerformanceMetric> performanceMetrics = generatePerformanceMetrics();
        List<Alert> alerts = generateAlerts();
        List<ActivityLog> activityLogs = generateActivityLogs();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("totalAuditLogs", totalAuditLogs);
        model.addAttribute("metrics", metrics);
        model.addAttribute("performanceMetrics", performanceMetrics);
        model.addAttribute("alerts", alerts);
        model.addAttribute("activityLogs", activityLogs);
        
        return "admin-monitoring";
    }
    
    private List<SystemMetric> generateSystemMetrics() {
        List<SystemMetric> metrics = new ArrayList<>();
        Random random = new Random();
        
        metrics.add(new SystemMetric("CPU Usage", random.nextInt(30) + 20 + "%", "Normal", "🟢"));
        metrics.add(new SystemMetric("Memory Usage", random.nextInt(40) + 30 + "%", "Normal", "🟢"));
        metrics.add(new SystemMetric("Disk Usage", random.nextInt(20) + 60 + "%", "Warning", "🟡"));
        metrics.add(new SystemMetric("Network I/O", random.nextInt(50) + 25 + " MB/s", "Normal", "🟢"));
        metrics.add(new SystemMetric("Database Connections", random.nextInt(20) + 15 + "", "Normal", "🟢"));
        metrics.add(new SystemMetric("Active Sessions", random.nextInt(30) + 10 + "", "Normal", "🟢"));
        
        return metrics;
    }
    
    private List<PerformanceMetric> generatePerformanceMetrics() {
        List<PerformanceMetric> metrics = new ArrayList<>();
        Random random = new Random();
        
        metrics.add(new PerformanceMetric("Response Time", random.nextInt(100) + 50 + "ms", "Good"));
        metrics.add(new PerformanceMetric("Throughput", random.nextInt(1000) + 500 + " req/s", "Good"));
        metrics.add(new PerformanceMetric("Error Rate", random.nextInt(5) + 1 + "%", "Acceptable"));
        metrics.add(new PerformanceMetric("Uptime", "99.9%", "Excellent"));
        
        return metrics;
    }
    
    private List<Alert> generateAlerts() {
        List<Alert> alerts = new ArrayList<>();
        
        alerts.add(new Alert("High Disk Usage", "Disk usage is approaching 80%", "Warning", "2024-01-15 14:30:00"));
        alerts.add(new Alert("Database Connection Pool", "Connection pool is 85% full", "Info", "2024-01-15 14:25:00"));
        alerts.add(new Alert("System Backup", "Daily backup completed successfully", "Success", "2024-01-15 14:00:00"));
        
        return alerts;
    }
    
    private List<ActivityLog> generateActivityLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        
        logs.add(new ActivityLog("User Login", "admin", "Successful login from 192.168.1.100", "2024-01-15 14:35:00"));
        logs.add(new ActivityLog("Transaction Created", "teller001", "Created transaction #TX12345", "2024-01-15 14:32:00"));
        logs.add(new ActivityLog("Account Opened", "cso001", "Opened account for customer John Doe", "2024-01-15 14:28:00"));
        logs.add(new ActivityLog("Loan Approved", "loan001", "Approved loan application #LOAN789", "2024-01-15 14:25:00"));
        logs.add(new ActivityLog("System Backup", "system", "Automated backup completed", "2024-01-15 14:00:00"));
        
        return logs;
    }
    
    public static class SystemMetric {
        private String name;
        private String value;
        private String status;
        private String icon;
        
        public SystemMetric(String name, String value, String status, String icon) {
            this.name = name;
            this.value = value;
            this.status = status;
            this.icon = icon;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
    
    public static class PerformanceMetric {
        private String name;
        private String value;
        private String status;
        
        public PerformanceMetric(String name, String value, String status) {
            this.name = name;
            this.value = value;
            this.status = status;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class Alert {
        private String title;
        private String message;
        private String type;
        private String timestamp;
        
        public Alert(String title, String message, String type, String timestamp) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
        }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    public static class ActivityLog {
        private String action;
        private String user;
        private String details;
        private String timestamp;
        
        public ActivityLog(String action, String user, String details, String timestamp) {
            this.action = action;
            this.user = user;
            this.details = details;
            this.timestamp = timestamp;
        }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}

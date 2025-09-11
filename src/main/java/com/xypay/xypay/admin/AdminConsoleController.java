package com.xypay.xypay.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/console")
public class AdminConsoleController {
    
    @GetMapping("/dashboard")
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("title", "XY Pay Admin Console");
        dashboard.put("version", "1.0");
        dashboard.put("modules", new String[]{
            "Customer & Account Setup",
            "Product Definition",
            "Interest & Charges Setup",
            "Transaction Rules",
            "Risk & Compliance",
            "Branch & Entity Setup",
            "Reporting & Analytics",
            "Workflow & Authorization",
            "Alerts & Notifications",
            "Integration Management",
            "Parameter Maintenance",
            "Database Query",
            "Scripting"
        });
        
        Map<String, String> shortcuts = new HashMap<>();
        shortcuts.put("customerSetup", "/admin/customer-account");
        shortcuts.put("productSetup", "/admin/product-setup");
        shortcuts.put("interestCharges", "/admin/interest-charges");
        shortcuts.put("riskCompliance", "/admin/risk-compliance");
        shortcuts.put("branchSetup", "/admin/risk-compliance");
        shortcuts.put("reporting", "/admin/reporting-workflow");
        shortcuts.put("workflow", "/admin/reporting-workflow");
        shortcuts.put("alerts", "/admin/alerts-integration");
        shortcuts.put("integration", "/admin/alerts-integration");
        shortcuts.put("parameters", "/admin/parameters");
        shortcuts.put("database", "/admin/database");
        shortcuts.put("scripting", "/admin/scripting");
        
        dashboard.put("shortcuts", shortcuts);
        return dashboard;
    }
    
    @GetMapping("/system-info")
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("applicationName", "XY Pay Core Banking System");
        systemInfo.put("version", "1.0.0");
        systemInfo.put("buildDate", "2023-01-01");
        systemInfo.put("environment", "Development");
        systemInfo.put("status", "Operational");
        return systemInfo;
    }
    
    @GetMapping("/tools")
    public Map<String, Object> getAdminTools() {
        Map<String, Object> tools = new HashMap<>();
        
        tools.put("parameterMaintenance", Map.of(
            "name", "Parameter Maintenance",
            "description", "Direct maintenance of system parameters",
            "endpoint", "/admin/parameters"
        ));
        
        tools.put("databaseQuery", Map.of(
            "name", "Database Query",
            "description", "Direct database query and update capabilities",
            "endpoint", "/admin/database"
        ));
        
        tools.put("scripting", Map.of(
            "name", "Scripting",
            "description", "Execute custom scripts for advanced logic",
            "endpoint", "/admin/scripting"
        ));
        
        return tools;
    }
}
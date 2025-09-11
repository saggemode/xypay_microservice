package com.xypay.xypay.admin;

import com.xypay.xypay.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class SuperuserDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/superuser/dashboard")
    public String superuserDashboard(Model model) {
        // Load dashboard metrics
        Map<String, Object> metrics = dashboardService.getDashboardMetrics();
        model.addAllAttributes(metrics);

        return "modern-dashboard";
    }

    @GetMapping("/superuser/dashboard/legacy")
    public String legacyDashboard() {
        return "superuser-dashboard";
    }

    @GetMapping("/superuser/dashboard/enterprise")
    public String enterpriseDashboard(Model model) {
        // Load dashboard metrics
        Map<String, Object> metrics = dashboardService.getDashboardMetrics();
        model.addAllAttributes(metrics);

        return "enterprise-dashboard";
    }
}

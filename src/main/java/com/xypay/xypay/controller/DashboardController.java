package com.xypay.xypay.controller;

import com.xypay.xypay.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/metrics")
    public Map<String, Object> getDashboardMetrics() {
        return dashboardService.getDashboardMetrics();
    }
    
    @GetMapping("/realtime")
    public Map<String, Object> getRealTimeMetrics() {
        return dashboardService.getRealTimeMetrics();
    }
    
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceMetrics() {
        return dashboardService.getPerformanceMetrics();
    }
}
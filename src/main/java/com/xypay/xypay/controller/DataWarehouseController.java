package com.xypay.xypay.controller;

import com.xypay.xypay.service.DataWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/datawarehouse")
public class DataWarehouseController {

    @Autowired
    private DataWarehouseService dataWarehouseService;

    @PostMapping("/etl/{date}")
    public ResponseEntity<String> runETL(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        dataWarehouseService.performDailyETL(date);
        return ResponseEntity.ok("ETL process completed for " + date);
    }

    @PostMapping("/etl/range")
    public ResponseEntity<String> runETLRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        dataWarehouseService.runETLForDateRange(startDate, endDate);
        return ResponseEntity.ok("ETL process completed for date range");
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getBusinessIntelligence(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> analytics = dataWarehouseService.getBusinessIntelligence(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyAnalytics(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        
        Map<String, Object> analytics = dataWarehouseService.getMonthlyAnalytics(year, month);
        return ResponseEntity.ok(analytics);
    }
}

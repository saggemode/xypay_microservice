package com.xypay.xypay.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalyticsService {
    public List<Map<String, Object>> exportAnalyticsJson() {
        return List.of(
            Map.of("customerId", 1, "name", "John Doe", "balance", 10000),
            Map.of("customerId", 2, "name", "Jane Smith", "balance", 5000)
        );
    }
    public String exportAnalyticsCsv() {
        return "customerId,name,balance\n1,John Doe,10000\n2,Jane Smith,5000\n";
    }
    public int importAnalyticsJson(List<Map<String, Object>> data) {
        return data.size();
    }
    public int importAnalyticsCsv(String csv) {
        return Math.max(0, csv.split("\\n").length - 1);
    }
}

package com.xypay.xypay.openbanking;

import com.xypay.xypay.service.AnalyticsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsConnectorController {
    private final AnalyticsService analyticsService;
    public AnalyticsConnectorController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> exportJson() {
        return analyticsService.exportAnalyticsJson();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public String exportCsv() {
        return analyticsService.exportAnalyticsCsv();
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> importJson(@RequestBody List<Map<String, Object>> data) {
        int imported = analyticsService.importAnalyticsJson(data);
        return ResponseEntity.ok("Imported " + imported + " records (JSON)");
    }

    @PostMapping(value = "/import", consumes = "text/csv")
    public ResponseEntity<String> importCsv(@RequestBody String csv) {
        int imported = analyticsService.importAnalyticsCsv(csv);
        return ResponseEntity.ok("Imported " + imported + " records (CSV)");
    }
}

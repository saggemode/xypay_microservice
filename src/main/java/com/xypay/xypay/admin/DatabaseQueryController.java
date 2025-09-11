package com.xypay.xypay.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/database")
public class DatabaseQueryController {
    
    @Autowired
    private DatabaseQueryService databaseQueryService;
    
    @PostMapping("/query")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            List<Map<String, Object>> results = databaseQueryService.executeQuery(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> executeUpdate(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            int affectedRows = databaseQueryService.executeUpdate(query);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("affectedRows", affectedRows);
            response.put("message", "Query executed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
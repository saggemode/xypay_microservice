package com.xypay.xypay.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/parameters")
public class ParameterMaintenanceController {
    
    @Autowired
    private ParameterMaintenanceService parameterMaintenanceService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllParameters() {
        Map<String, Object> parameters = parameterMaintenanceService.getAllParameters();
        return ResponseEntity.ok(parameters);
    }
    
    @GetMapping("/{paramName}")
    public ResponseEntity<Object> getParameter(@PathVariable String paramName) {
        Object parameter = parameterMaintenanceService.getParameter(paramName);
        if (parameter != null) {
            return ResponseEntity.ok(parameter);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Object> setParameter(@RequestBody Map<String, Object> paramData) {
        String paramName = (String) paramData.get("name");
        Object paramValue = paramData.get("value");
        
        if (paramName != null && paramValue != null) {
            parameterMaintenanceService.setParameter(paramName, paramValue);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{paramName}")
    public ResponseEntity<Object> updateParameter(@PathVariable String paramName, @RequestBody Map<String, Object> paramData) {
        Object paramValue = paramData.get("value");
        
        if (paramValue != null) {
            parameterMaintenanceService.setParameter(paramName, paramValue);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{paramName}")
    public ResponseEntity<Object> deleteParameter(@PathVariable String paramName) {
        parameterMaintenanceService.deleteParameter(paramName);
        return ResponseEntity.ok().build();
    }
}
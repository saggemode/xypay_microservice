package com.xypay.xypay.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.script.ScriptException;
import java.util.Map;

@RestController
@RequestMapping("/admin/scripting")
public class ScriptingController {
    
    @Autowired
    private ScriptingService scriptingService;
    
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeScript(@RequestBody Map<String, Object> request) {
        String script = (String) request.get("script");
        Map<String, Object> context = (Map<String, Object>) request.get("context");
        
        if (script == null || script.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Object result;
            if (context != null && !context.isEmpty()) {
                result = scriptingService.executeScriptWithContext(script, context);
            } else {
                result = scriptingService.executeScript(script);
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("result", result);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (ScriptException e) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("error", e.getMessage());
            response.put("success", false);
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateScript(@RequestBody Map<String, String> request) {
        String script = request.get("script");
        
        if (script == null || script.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean isValid = scriptingService.validateScript(script);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("valid", isValid);
        
        if (!isValid) {
            response.put("message", "Script contains syntax errors");
        } else {
            response.put("message", "Script is valid");
        }
        
        return ResponseEntity.ok(response);
    }
}
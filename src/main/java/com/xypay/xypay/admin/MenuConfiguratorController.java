package com.xypay.xypay.admin;

import com.xypay.xypay.domain.MenuConfiguration;
import com.xypay.xypay.service.MenuConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/menu-configurator")
public class MenuConfiguratorController {
    
    @Autowired
    private MenuConfigurationService menuConfigurationService;
    
    /**
     * Create menu configuration
     */
    @PostMapping("/menu-config")
    public ResponseEntity<Map<String, Object>> createMenuConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = menuConfigurationService.createMenuConfiguration(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get menu configuration by code
     */
    @GetMapping("/menu-config/{menuCode}")
    public ResponseEntity<MenuConfiguration> getMenuConfig(@PathVariable String menuCode) {
        MenuConfiguration config = menuConfigurationService.getMenuConfiguration(menuCode);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }
    
    /**
     * Get all menu configurations
     */
    @GetMapping("/menu-config")
    public ResponseEntity<List<MenuConfiguration>> getAllMenuConfigs() {
        List<MenuConfiguration> configs = menuConfigurationService.getAllMenuConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Get menu tree for specific role
     */
    @GetMapping("/menu-tree/{role}")
    public ResponseEntity<Map<String, Object>> getMenuTree(@PathVariable String role) {
        Map<String, Object> menuTree = menuConfigurationService.generateMenuTree(role);
        return ResponseEntity.ok(menuTree);
    }
    
    /**
     * Update menu configuration
     */
    @PutMapping("/menu-config/{id}")
    public ResponseEntity<Map<String, Object>> updateMenuConfig(@PathVariable UUID id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = menuConfigurationService.updateMenuConfiguration(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete menu configuration
     */
    @DeleteMapping("/menu-config/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenuConfig(@PathVariable UUID id) {
        Map<String, Object> response = menuConfigurationService.deleteMenuConfiguration(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reorder menu items
     */
    @PostMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorderMenus(@RequestBody Map<String, Object> orderData) {
        Map<String, Object> response = menuConfigurationService.reorderMenuItems(orderData);
        return ResponseEntity.ok(response);
    }
}

package com.xypay.xypay.service;

import com.xypay.xypay.domain.MenuConfiguration;
import com.xypay.xypay.repository.MenuConfigurationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class MenuConfigurationService {
    
    @Autowired
    private MenuConfigurationRepository menuConfigurationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> createMenuConfiguration(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            MenuConfiguration config = new MenuConfiguration();
            config.setMenuCode((String) request.get("menuCode"));
            config.setMenuName((String) request.get("menuName"));
            config.setParentMenuCode((String) request.get("parentMenuCode"));
            config.setMenuLevel((Integer) request.get("menuLevel"));
            config.setDisplayOrder((Integer) request.get("displayOrder"));
            config.setMenuUrl((String) request.get("menuUrl"));
            config.setMenuIcon((String) request.get("menuIcon"));
            config.setRequiredRoles(objectMapper.writeValueAsString(request.get("requiredRoles")));
            config.setRequiredPermissions(objectMapper.writeValueAsString(request.get("requiredPermissions")));
            config.setMenuType((String) request.get("menuType"));
            config.setIsVisible((Boolean) request.getOrDefault("isVisible", true));
            config.setIsEnabled((Boolean) request.getOrDefault("isEnabled", true));
            config.setTooltipText((String) request.get("tooltipText"));
            config.setCssClass((String) request.get("cssClass"));
            config.setTargetWindow((String) request.getOrDefault("targetWindow", "_self"));
            
            config = menuConfigurationRepository.save(config);
            
            response.put("success", true);
            response.put("message", "Menu configuration created successfully");
            response.put("menuId", config.getId());
            response.put("menuCode", config.getMenuCode());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create menu configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public MenuConfiguration getMenuConfiguration(String menuCode) {
        return menuConfigurationRepository.findByMenuCode(menuCode).orElse(null);
    }
    
    public List<MenuConfiguration> getAllMenuConfigurations() {
        return menuConfigurationRepository.findAll();
    }
    
    public Map<String, Object> updateMenuConfiguration(UUID id, Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<MenuConfiguration> configOpt = menuConfigurationRepository.findById(id);
            if (configOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Menu configuration not found");
                return response;
            }
            
            MenuConfiguration config = configOpt.get();
            if (request.containsKey("menuName")) config.setMenuName((String) request.get("menuName"));
            if (request.containsKey("parentMenuCode")) config.setParentMenuCode((String) request.get("parentMenuCode"));
            if (request.containsKey("menuLevel")) config.setMenuLevel((Integer) request.get("menuLevel"));
            if (request.containsKey("displayOrder")) config.setDisplayOrder((Integer) request.get("displayOrder"));
            if (request.containsKey("menuUrl")) config.setMenuUrl((String) request.get("menuUrl"));
            if (request.containsKey("menuIcon")) config.setMenuIcon((String) request.get("menuIcon"));
            if (request.containsKey("requiredRoles")) config.setRequiredRoles(objectMapper.writeValueAsString(request.get("requiredRoles")));
            if (request.containsKey("requiredPermissions")) config.setRequiredPermissions(objectMapper.writeValueAsString(request.get("requiredPermissions")));
            if (request.containsKey("menuType")) config.setMenuType((String) request.get("menuType"));
            if (request.containsKey("isVisible")) config.setIsVisible((Boolean) request.get("isVisible"));
            if (request.containsKey("isEnabled")) config.setIsEnabled((Boolean) request.get("isEnabled"));
            if (request.containsKey("tooltipText")) config.setTooltipText((String) request.get("tooltipText"));
            if (request.containsKey("cssClass")) config.setCssClass((String) request.get("cssClass"));
            if (request.containsKey("targetWindow")) config.setTargetWindow((String) request.get("targetWindow"));
            
            config = menuConfigurationRepository.save(config);
            
            response.put("success", true);
            response.put("message", "Menu configuration updated successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update menu configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> deleteMenuConfiguration(UUID id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (menuConfigurationRepository.existsById(id)) {
                menuConfigurationRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "Menu configuration deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Menu configuration not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete menu configuration: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> generateMenuTree(String userRole) {
        Map<String, Object> menuTree = new HashMap<>();
        
        try {
            List<MenuConfiguration> allMenus = menuConfigurationRepository.findByIsVisibleAndIsEnabled(true, true);
            
            // Filter menus based on user role
            List<MenuConfiguration> accessibleMenus = allMenus.stream()
                .filter(menu -> hasAccess(menu, userRole))
                .sorted(Comparator.comparing(MenuConfiguration::getDisplayOrder))
                .collect(Collectors.toList());
            
            // Build hierarchical structure
            Map<String, List<MenuConfiguration>> menusByParent = accessibleMenus.stream()
                .collect(Collectors.groupingBy(menu -> 
                    menu.getParentMenuCode() != null ? menu.getParentMenuCode() : "ROOT"));
            
            List<Map<String, Object>> rootMenus = new ArrayList<>();
            
            if (menusByParent.containsKey("ROOT")) {
                for (MenuConfiguration rootMenu : menusByParent.get("ROOT")) {
                    Map<String, Object> menuItem = buildMenuTree(rootMenu, menusByParent);
                    rootMenus.add(menuItem);
                }
            }
            
            menuTree.put("success", true);
            menuTree.put("menus", rootMenus);
            menuTree.put("userRole", userRole);
            
        } catch (Exception e) {
            menuTree.put("success", false);
            menuTree.put("message", "Failed to generate menu tree: " + e.getMessage());
        }
        
        return menuTree;
    }
    
    private boolean hasAccess(MenuConfiguration menu, String userRole) {
        try {
            if (menu.getRequiredRoles() == null || menu.getRequiredRoles().isEmpty()) {
                return true; // No role restriction
            }
            
            @SuppressWarnings("unchecked")
            List<String> requiredRoles = objectMapper.readValue(menu.getRequiredRoles(), List.class);
            return requiredRoles.contains(userRole) || requiredRoles.contains("ROLE_" + userRole);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private Map<String, Object> buildMenuTree(MenuConfiguration menu, Map<String, List<MenuConfiguration>> menusByParent) {
        Map<String, Object> menuItem = new HashMap<>();
        
        menuItem.put("menuCode", menu.getMenuCode());
        menuItem.put("menuName", menu.getMenuName());
        menuItem.put("menuUrl", menu.getMenuUrl());
        menuItem.put("menuIcon", menu.getMenuIcon());
        menuItem.put("menuType", menu.getMenuType());
        menuItem.put("tooltipText", menu.getTooltipText());
        menuItem.put("cssClass", menu.getCssClass());
        menuItem.put("targetWindow", menu.getTargetWindow());
        
        // Add children if any
        if (menusByParent.containsKey(menu.getMenuCode())) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (MenuConfiguration childMenu : menusByParent.get(menu.getMenuCode())) {
                children.add(buildMenuTree(childMenu, menusByParent));
            }
            menuItem.put("children", children);
        }
        
        return menuItem;
    }
    
    public Map<String, Object> reorderMenuItems(Map<String, Object> orderData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> menuOrders = (List<Map<String, Object>>) orderData.get("menuOrders");
            
            for (Map<String, Object> menuOrder : menuOrders) {
                String menuCode = (String) menuOrder.get("menuCode");
                Integer newOrder = (Integer) menuOrder.get("displayOrder");
                
                Optional<MenuConfiguration> menuOpt = menuConfigurationRepository.findByMenuCode(menuCode);
                if (menuOpt.isPresent()) {
                    MenuConfiguration menu = menuOpt.get();
                    menu.setDisplayOrder(newOrder);
                    menuConfigurationRepository.save(menu);
                }
            }
            
            response.put("success", true);
            response.put("message", "Menu items reordered successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to reorder menu items: " + e.getMessage());
        }
        
        return response;
    }
}

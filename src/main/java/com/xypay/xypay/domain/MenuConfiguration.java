package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "menu_configurations")
public class MenuConfiguration extends BaseEntity {
    
    @Column(name = "menu_code", unique = true, nullable = false)
    private String menuCode;
    
    @Column(name = "menu_name", nullable = false)
    private String menuName;
    
    @Column(name = "parent_menu_code")
    private String parentMenuCode;
    
    @Column(name = "menu_level")
    private Integer menuLevel = 1;
    
    @Column(name = "display_order")
    private Integer displayOrder = 1;
    
    @Column(name = "menu_url")
    private String menuUrl;
    
    @Column(name = "menu_icon")
    private String menuIcon;
    
    @Column(name = "required_roles", columnDefinition = "TEXT")
    private String requiredRoles; // JSON array of roles
    
    @Column(name = "required_permissions", columnDefinition = "TEXT")
    private String requiredPermissions; // JSON array of permissions
    
    @Column(name = "menu_type")
    private String menuType; // MENU, ACTION, SEPARATOR, HEADER
    
    @Column(name = "is_visible")
    private Boolean isVisible = true;
    
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    
    @Column(name = "tooltip_text")
    private String tooltipText;
    
    @Column(name = "css_class")
    private String cssClass;
    
    @Column(name = "target_window")
    private String targetWindow = "_self"; // _self, _blank, _parent, _top
}

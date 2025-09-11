package com.xypay.xypay.domain;

import jakarta.persistence.*;

@Entity
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Lob
    private String dashboardConfig; // JSON string for widget layout, theme, etc.

    @Lob
    private String notificationConfig; // JSON string for notification settings

    @Lob
    private String workflowConfig; // JSON string for workflow customizations

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDashboardConfig() { return dashboardConfig; }
    public void setDashboardConfig(String dashboardConfig) { this.dashboardConfig = dashboardConfig; }
    public String getNotificationConfig() { return notificationConfig; }
    public void setNotificationConfig(String notificationConfig) { this.notificationConfig = notificationConfig; }
    public String getWorkflowConfig() { return workflowConfig; }
    public void setWorkflowConfig(String workflowConfig) { this.workflowConfig = workflowConfig; }
}

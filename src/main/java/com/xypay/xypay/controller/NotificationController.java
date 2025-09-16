package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.domain.NotificationStatus;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.service.NotificationService;
import com.xypay.xypay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Notification management.
 * Equivalent to Django notification viewsets with full CRUD operations.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get current user from authentication context.
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        // Fallback for testing - in production this should throw an exception
        User testUser = new User();
        testUser.setId(java.util.UUID.randomUUID());
        testUser.setUsername("testuser");
        return testUser;
    }
    
    /**
     * Get notifications for the current user with pagination and filtering.
     */
    @GetMapping
    @Cacheable(value = "userNotifications", key = "#authentication.name + '_' + #page + '_' + #size + '_' + #type + '_' + #level + '_' + #status")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationLevel level,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Notification> notifications;
            
            if (type != null) {
                notifications = notificationService.getUserNotificationsByType(currentUser, type, pageable);
            } else if (level != null) {
                notifications = notificationService.getUserNotificationsByLevel(currentUser, level, pageable);
            } else if (status != null) {
                notifications = notificationService.getUserNotificationsByStatus(currentUser, status, pageable);
            } else {
                notifications = notificationService.getUserNotifications(currentUser, pageable);
            }
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching user notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific notification by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotification(
            Authentication authentication,
            @PathVariable Long id) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            Notification notification = notificationService.getNotificationById(id);
            
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user owns this notification
            if (!notification.getRecipient().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            logger.error("Error fetching notification: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get unread notifications count for the current user.
     */
    @GetMapping("/unread-count")
    @Cacheable(value = "unreadCount", key = "#authentication.name")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationsCount(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            long count = notificationService.countUnreadNotifications(currentUser);
            
            Map<String, Long> response = new HashMap<>();
            response.put("unreadCount", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching unread count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get unread notifications for the current user.
     */
    @GetMapping("/unread")
    @Cacheable(value = "unreadNotifications", key = "#authentication.name")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<Notification> notifications = notificationService.getUnreadNotifications(currentUser);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching unread notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get urgent notifications for the current user.
     */
    @GetMapping("/urgent")
    public ResponseEntity<List<Notification>> getUrgentNotifications(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<Notification> notifications = notificationService.getUrgentNotifications(currentUser);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching urgent notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get actionable notifications for the current user.
     */
    @GetMapping("/actionable")
    public ResponseEntity<List<Notification>> getActionableNotifications(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<Notification> notifications = notificationService.getActionableNotifications(currentUser);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching actionable notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark a notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            Authentication authentication,
            @PathVariable Long id) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            Notification notification = notificationService.markAsRead(id);
            
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user owns this notification
            if (!notification.getRecipient().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            logger.error("Error marking notification as read: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark a notification as unread.
     */
    @PutMapping("/{id}/unread")
    public ResponseEntity<Notification> markAsUnread(
            Authentication authentication,
            @PathVariable Long id) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            Notification notification = notificationService.markAsUnread(id);
            
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user owns this notification
            if (!notification.getRecipient().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            logger.error("Error marking notification as unread: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark all notifications as read for the current user.
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            int updatedCount = notificationService.markAllAsRead(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedCount", updatedCount);
            response.put("message", "All notifications marked as read");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error marking all notifications as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Bulk mark notifications as read.
     */
    @PutMapping("/bulk-read")
    public ResponseEntity<Map<String, Object>> bulkMarkAsRead(
            Authentication authentication,
            @RequestBody List<Long> notificationIds) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            int updatedCount = notificationService.bulkMarkAsRead(notificationIds, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedCount", updatedCount);
            response.put("message", "Notifications marked as read");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error bulk marking notifications as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Bulk mark notifications as unread.
     */
    @PutMapping("/bulk-unread")
    public ResponseEntity<Map<String, Object>> bulkMarkAsUnread(
            Authentication authentication,
            @RequestBody List<Long> notificationIds) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            int updatedCount = notificationService.bulkMarkAsUnread(notificationIds, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedCount", updatedCount);
            response.put("message", "Notifications marked as unread");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error bulk marking notifications as unread", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete a notification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            Authentication authentication,
            @PathVariable Long id) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            boolean deleted = notificationService.deleteNotification(id, currentUser);
            
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting notification: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notification statistics for the current user.
     */
    @GetMapping("/stats")
    @Cacheable(value = "notificationStats", key = "#authentication.name")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Map<String, Object> stats = notificationService.getNotificationStatistics(currentUser);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching notification statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get notifications by date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Notification>> getNotificationsByDateRange(
            Authentication authentication,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        try {
            User currentUser = getCurrentUser(authentication);
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            List<Notification> notifications = notificationService.getNotificationsByDateRange(
                currentUser, start, end);
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error fetching notifications by date range", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create a new notification (admin only).
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(
            Authentication authentication,
            @RequestBody Map<String, Object> notificationData) {
        
        try {
            // In a real implementation, check for admin role
            getCurrentUser(authentication);
            
            Notification notification = notificationService.createNotificationFromData(notificationData);
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (Exception e) {
            logger.error("Error creating notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
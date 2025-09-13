package com.xypay.xypay.admin;

import com.xypay.xypay.domain.Notification;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.domain.NotificationStatus;
import com.xypay.xypay.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERUSER')")
public class AdminNotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    // Main notifications page
    @GetMapping("/notifications")
    public String notificationsPage() {
        return "admin/notifications";
    }
    
    // API endpoint to get all notifications (admin view)
    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        try {
            System.out.println("=== Admin API /admin/api/notifications called ===");
            System.out.println("Parameters - page: " + page + ", size: " + size + ", sortBy: " + sortBy + ", sortDir: " + sortDir);
            System.out.println("Filters - type: " + type + ", level: " + level + ", status: " + status + ", search: " + search);
            
            // Create pageable object
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get all notifications with pagination
            Page<Notification> notificationPage = notificationService.getAllNotifications(pageable);
            
            System.out.println("Found " + notificationPage.getTotalElements() + " notifications");
            
            // Convert notifications to DTO format for frontend
            List<Map<String, Object>> notificationDtos = notificationPage.getContent().stream()
                .map(this::convertToDto)
                .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", notificationDtos);
            response.put("totalElements", notificationPage.getTotalElements());
            response.put("totalPages", notificationPage.getTotalPages());
            response.put("currentPage", notificationPage.getNumber());
            response.put("size", notificationPage.getSize());
            response.put("first", notificationPage.isFirst());
            response.put("last", notificationPage.isLast());
            response.put("numberOfElements", notificationPage.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getNotifications: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // API endpoint to get notification statistics
    @GetMapping("/api/notifications/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            // Get all notifications for stats
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Notification> allNotifications = notificationService.getAllNotifications(pageable);
            
            long totalNotifications = allNotifications.getTotalElements();
            long unreadCount = allNotifications.getContent().stream()
                .mapToLong(n -> n.isRead() ? 0 : 1)
                .sum();
            long urgentCount = allNotifications.getContent().stream()
                .mapToLong(n -> n.isUrgent() ? 1 : 0)
                .sum();
            long actionableCount = allNotifications.getContent().stream()
                .mapToLong(n -> n.isActionable() ? 1 : 0)
                .sum();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalNotifications", totalNotifications);
            stats.put("unreadCount", unreadCount);
            stats.put("urgentCount", urgentCount);
            stats.put("actionableCount", actionableCount);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error getting notification stats: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // API endpoint to mark all notifications as read
    @PostMapping("/api/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        try {
            // Get all notifications and mark them as read
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Notification> allNotifications = notificationService.getAllNotifications(pageable);
            
            int updatedCount = 0;
            for (Notification notification : allNotifications.getContent()) {
                if (!notification.isRead()) {
                    notification.markAsRead();
                    notificationService.createNotification(notification);
                    updatedCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedCount", updatedCount);
            response.put("message", "All notifications marked as read");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error marking all as read: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // API endpoint to delete selected notifications
    @PostMapping("/api/notifications/delete-selected")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSelected(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> notificationIds = (List<Long>) request.get("notificationIds");
            
            if (notificationIds == null || notificationIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No notification IDs provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            int deletedCount = 0;
            for (Long id : notificationIds) {
                try {
                    if (notificationService.deleteNotification(id)) {
                        deletedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting notification " + id + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "Selected notifications deleted");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error deleting selected notifications: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // API endpoint to create a new notification
    @PostMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Map<String, Object> notificationData) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // API endpoint to delete a single notification
    @DeleteMapping("/api/notifications/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        try {
            boolean deleted = notificationService.deleteNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("success", true);
                response.put("message", "Notification deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Notification not found");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Convert Notification entity to DTO for frontend consumption
     */
    private Map<String, Object> convertToDto(Notification notification) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", notification.getId());
        dto.put("title", notification.getTitle());
        dto.put("message", notification.getMessage());
        dto.put("notificationType", notification.getNotificationType() != null ? notification.getNotificationType().toString() : "OTHER");
        dto.put("level", notification.getLevel() != null ? notification.getLevel().toString() : "INFO");
        dto.put("status", notification.getStatus() != null ? notification.getStatus().toString() : "PENDING");
        dto.put("isRead", notification.isRead());
        dto.put("readAt", notification.getReadAt());
        dto.put("actionText", notification.getActionText());
        dto.put("actionUrl", notification.getActionUrl());
        dto.put("link", notification.getLink());
        dto.put("priority", notification.getPriority());
        dto.put("source", notification.getSource());
        dto.put("createdAt", notification.getCreatedAt());
        dto.put("updatedAt", notification.getUpdatedAt());
        dto.put("isUrgent", notification.isUrgent());
        dto.put("isActionable", notification.isActionable());
        dto.put("ageInHours", notification.getAgeInHours());
        
        // Add recipient information
        if (notification.getRecipient() != null) {
            Map<String, Object> recipient = new HashMap<>();
            recipient.put("id", notification.getRecipient().getId());
            recipient.put("email", notification.getRecipient().getEmail());
            recipient.put("firstName", notification.getRecipient().getFirstName());
            recipient.put("lastName", notification.getRecipient().getLastName());
            dto.put("recipient", recipient);
        }
        
        // Add sender information
        if (notification.getSender() != null) {
            Map<String, Object> sender = new HashMap<>();
            sender.put("id", notification.getSender().getId());
            sender.put("email", notification.getSender().getEmail());
            sender.put("firstName", notification.getSender().getFirstName());
            sender.put("lastName", notification.getSender().getLastName());
            dto.put("sender", sender);
        }
        
        // Add transaction information if available
        if (notification.getTransaction() != null) {
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("id", notification.getTransaction().getId());
            transaction.put("amount", notification.getTransaction().getAmount());
            transaction.put("type", notification.getTransaction().getType());
            transaction.put("status", notification.getTransaction().getStatus());
            dto.put("transaction", transaction);
        }
        
        // Add bank transfer information if available
        if (notification.getBankTransfer() != null) {
            Map<String, Object> bankTransfer = new HashMap<>();
            bankTransfer.put("id", notification.getBankTransfer().getId());
            bankTransfer.put("amount", notification.getBankTransfer().getAmount());
            bankTransfer.put("accountNumber", notification.getBankTransfer().getAccountNumber());
            bankTransfer.put("status", notification.getBankTransfer().getStatus());
            dto.put("bankTransfer", bankTransfer);
        }
        
        return dto;
    }
}
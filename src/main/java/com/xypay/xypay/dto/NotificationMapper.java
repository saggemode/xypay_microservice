package com.xypay.xypay.dto;

import com.xypay.xypay.domain.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    
    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setNotificationType(notification.getNotificationType());
        dto.setLevel(notification.getLevel());
        dto.setStatus(notification.getStatus());
        dto.setRead(notification.isRead());
        dto.setReadAt(notification.getReadAt());
        dto.setActionText(notification.getActionText());
        dto.setActionUrl(notification.getActionUrl());
        dto.setLink(notification.getLink());
        dto.setPriority(notification.getPriority());
        dto.setSource(notification.getSource());
        dto.setExtraData(notification.getExtraData());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        
        if (notification.getRecipient() != null) {
            dto.setRecipientUsername(notification.getRecipient().getUsername());
        }
        
        return dto;
    }
}
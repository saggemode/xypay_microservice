package com.xypay.customer.service;

import com.xypay.customer.domain.*;
import com.xypay.customer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GDPRService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    
    @Autowired
    private TicketCommentRepository ticketCommentRepository;
    
    @Autowired
    private TicketAttachmentRepository ticketAttachmentRepository;
    
    @Autowired
    private DataConsentRepository dataConsentRepository;
    
    // Data Export
    public Map<String, Object> exportCustomerData(Long customerId) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Map<String, Object> exportData = new HashMap<>();
        
        // Basic customer information
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("id", customer.getId());
        customerInfo.put("username", customer.getUsername());
        customerInfo.put("firstName", customer.getFirstName());
        customerInfo.put("lastName", customer.getLastName());
        customerInfo.put("email", customer.getEmail());
        customerInfo.put("roles", customer.getRoles());
        customerInfo.put("enabled", customer.isEnabled());
        customerInfo.put("createdAt", customer.getCreatedAt());
        exportData.put("customer", customerInfo);
        
        // User profile information
        Optional<UserProfile> profile = userProfileRepository.findByUserId(customerId);
        if (profile.isPresent()) {
            UserProfile userProfile = profile.get();
            Map<String, Object> profileInfo = new HashMap<>();
            profileInfo.put("phone", userProfile.getPhone());
            profileInfo.put("isVerified", userProfile.getIsVerified());
            profileInfo.put("emailVerified", userProfile.getEmailVerified());
            profileInfo.put("notifyEmail", userProfile.getNotifyEmail());
            profileInfo.put("notifySms", userProfile.getNotifySms());
            profileInfo.put("notifyPush", userProfile.getNotifyPush());
            profileInfo.put("notifyInApp", userProfile.getNotifyInApp());
            profileInfo.put("createdAt", userProfile.getCreatedAt());
            profileInfo.put("updatedAt", userProfile.getUpdatedAt());
            exportData.put("profile", profileInfo);
        }
        
        // KYC information
        Optional<KYCProfile> kyc = kycProfileRepository.findByUserId(customerId);
        if (kyc.isPresent()) {
            KYCProfile kycProfile = kyc.get();
            Map<String, Object> kycInfo = new HashMap<>();
            kycInfo.put("kycLevel", kycProfile.getKycLevel());
            kycInfo.put("isApproved", kycProfile.getIsApproved());
            kycInfo.put("dateOfBirth", kycProfile.getDateOfBirth());
            kycInfo.put("state", kycProfile.getState());
            kycInfo.put("lga", kycProfile.getLga());
            kycInfo.put("area", kycProfile.getArea());
            kycInfo.put("address", kycProfile.getAddress());
            kycInfo.put("telephoneNumber", kycProfile.getTelephoneNumber());
            kycInfo.put("createdAt", kycProfile.getCreatedAt());
            kycInfo.put("updatedAt", kycProfile.getUpdatedAt());
            exportData.put("kyc", kycInfo);
        }
        
        // Support tickets
        List<SupportTicket> tickets = supportTicketRepository.findByCustomer(customer);
        List<Map<String, Object>> ticketsInfo = tickets.stream().map(ticket -> {
            Map<String, Object> ticketInfo = new HashMap<>();
            ticketInfo.put("ticketNumber", ticket.getTicketNumber());
            ticketInfo.put("subject", ticket.getSubject());
            ticketInfo.put("description", ticket.getDescription());
            ticketInfo.put("category", ticket.getCategory());
            ticketInfo.put("priority", ticket.getPriority());
            ticketInfo.put("status", ticket.getStatus());
            ticketInfo.put("createdAt", ticket.getCreatedAt());
            ticketInfo.put("resolvedAt", ticket.getResolvedAt());
            ticketInfo.put("customerSatisfactionRating", ticket.getCustomerSatisfactionRating());
            ticketInfo.put("customerFeedback", ticket.getCustomerFeedback());
            return ticketInfo;
        }).collect(Collectors.toList());
        exportData.put("supportTickets", ticketsInfo);
        
        // Data consents
        List<DataConsent> consents = dataConsentRepository.findByUser(customer);
        List<Map<String, Object>> consentsInfo = consents.stream().map(consent -> {
            Map<String, Object> consentInfo = new HashMap<>();
            consentInfo.put("consentType", consent.getConsentType());
            consentInfo.put("status", consent.getStatus());
            consentInfo.put("consentGivenAt", consent.getConsentGivenAt());
            consentInfo.put("consentWithdrawnAt", consent.getConsentWithdrawnAt());
            consentInfo.put("expiresAt", consent.getExpiresAt());
            consentInfo.put("consentVersion", consent.getConsentVersion());
            return consentInfo;
        }).collect(Collectors.toList());
        exportData.put("dataConsents", consentsInfo);
        
        // Export metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exportDate", LocalDateTime.now());
        metadata.put("dataVersion", "1.0");
        metadata.put("format", "JSON");
        exportData.put("metadata", metadata);
        
        return exportData;
    }
    
    // Data Deletion (Right to be Forgotten)
    public void deleteCustomerData(Long customerId) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Delete support ticket attachments (files)
        List<SupportTicket> tickets = supportTicketRepository.findByCustomer(customer);
        for (SupportTicket ticket : tickets) {
            List<TicketAttachment> attachments = ticketAttachmentRepository.findByTicket(ticket);
            for (TicketAttachment attachment : attachments) {
                // Delete physical files here
                ticketAttachmentRepository.delete(attachment);
            }
        }
        
        // Delete support ticket comments
        for (SupportTicket ticket : tickets) {
            List<TicketComment> comments = ticketCommentRepository.findByTicket(ticket);
            ticketCommentRepository.deleteAll(comments);
        }
        
        // Delete support tickets
        supportTicketRepository.deleteAll(tickets);
        
        // Delete data consents
        List<DataConsent> consents = dataConsentRepository.findByUser(customer);
        dataConsentRepository.deleteAll(consents);
        
        // Delete KYC profile
        Optional<KYCProfile> kyc = kycProfileRepository.findByUserId(customerId);
        kyc.ifPresent(kycProfileRepository::delete);
        
        // Delete user profile
        Optional<UserProfile> profile = userProfileRepository.findByUserId(customerId);
        profile.ifPresent(userProfileRepository::delete);
        
        // Anonymize user data instead of deleting (for audit purposes)
        customer.setUsername("DELETED_" + customer.getId());
        customer.setEmail("deleted_" + customer.getId() + "@deleted.com");
        customer.setFirstName("DELETED");
        customer.setLastName("USER");
        customer.setEnabled(false);
        userRepository.save(customer);
    }
    
    // Data Portability
    public Map<String, Object> getDataPortability(Long customerId) {
        return exportCustomerData(customerId);
    }
    
    // Consent Management
    public DataConsent grantConsent(Long customerId, DataConsent.ConsentType consentType, 
                                   String ipAddress, String userAgent, String consentVersion) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        DataConsent consent = new DataConsent(customer, consentType, DataConsent.ConsentStatus.GRANTED);
        consent.grantConsent(ipAddress, userAgent, consentVersion);
        
        return dataConsentRepository.save(consent);
    }
    
    public DataConsent withdrawConsent(Long customerId, DataConsent.ConsentType consentType) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        DataConsent consent = dataConsentRepository.findByUserAndConsentType(customer, consentType)
            .orElseThrow(() -> new RuntimeException("Consent not found"));
        
        consent.withdrawConsent();
        return dataConsentRepository.save(consent);
    }
    
    public List<DataConsent> getCustomerConsents(Long customerId) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        return dataConsentRepository.findByUser(customer);
    }
    
    public boolean hasActiveConsent(Long customerId, DataConsent.ConsentType consentType) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Optional<DataConsent> consent = dataConsentRepository.findByUserAndConsentType(customer, consentType);
        return consent.isPresent() && consent.get().isActive();
    }
    
    // Data Retention
    public void cleanupExpiredData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(7); // 7 years retention
        
        // Find and anonymize old inactive users
        List<User> oldUsers = userRepository.findOldInactiveUsers(cutoffDate);
        for (User user : oldUsers) {
            if (!user.isEnabled()) {
                deleteCustomerData(user.getId());
            }
        }
        
        // Clean up expired consents
        List<DataConsent> expiredConsents = dataConsentRepository.findExpiredConsents(LocalDateTime.now());
        for (DataConsent consent : expiredConsents) {
            consent.setStatus(DataConsent.ConsentStatus.EXPIRED);
            dataConsentRepository.save(consent);
        }
    }
    
    // Audit Trail
    public void logDataAccess(Long customerId, String action, String details, String accessedBy) {
        // Implementation would log to audit system
        // This could be integrated with a logging service or audit database
        System.out.println(String.format("Data Access Log: Customer %d, Action: %s, Details: %s, Accessed By: %s, Time: %s", 
            customerId, action, details, accessedBy, LocalDateTime.now()));
    }
}

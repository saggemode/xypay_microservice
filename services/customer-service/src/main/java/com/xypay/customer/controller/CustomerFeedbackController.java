package com.xypay.customer.controller;

import com.xypay.customer.domain.CustomerFeedback;
import com.xypay.customer.service.CustomerFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-feedback")
public class CustomerFeedbackController {
    
    @Autowired
    private CustomerFeedbackService customerFeedbackService;
    
    @PostMapping
    public ResponseEntity<CustomerFeedback> submitFeedback(
            @RequestBody Map<String, Object> feedbackData,
            HttpServletRequest request) {
        
        Long customerId = Long.valueOf(feedbackData.get("customerId").toString());
        CustomerFeedback.FeedbackType feedbackType = CustomerFeedback.FeedbackType.valueOf(
            feedbackData.get("feedbackType").toString().toUpperCase());
        String title = (String) feedbackData.get("title");
        String description = (String) feedbackData.get("description");
        Integer rating = feedbackData.get("rating") != null ? Integer.valueOf(feedbackData.get("rating").toString()) : null;
        
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        CustomerFeedback feedback = customerFeedbackService.submitFeedback(
            customerId, feedbackType, title, description, rating, ipAddress, userAgent);
        
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerFeedback> getFeedback(@PathVariable Long id) {
        CustomerFeedback feedback = customerFeedbackService.getFeedbackById(id);
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerFeedback>> getCustomerFeedback(@PathVariable Long customerId) {
        List<CustomerFeedback> feedback = customerFeedbackService.getFeedbackByCustomer(customerId);
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<CustomerFeedback>> getPublicFeedback() {
        List<CustomerFeedback> feedback = customerFeedbackService.getPublicFeedback();
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}/assign")
    public ResponseEntity<CustomerFeedback> assignFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> assignData) {
        
        String assignedTo = assignData.get("assignedTo");
        CustomerFeedback feedback = customerFeedbackService.assignFeedback(id, assignedTo);
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<CustomerFeedback> acknowledgeFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> responseData) {
        
        String response = responseData.get("response");
        CustomerFeedback feedback = customerFeedbackService.acknowledgeFeedback(id, response);
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}/resolve")
    public ResponseEntity<CustomerFeedback> resolveFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> responseData) {
        
        String response = responseData.get("response");
        CustomerFeedback feedback = customerFeedbackService.resolveFeedback(id, response);
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}/escalate")
    public ResponseEntity<CustomerFeedback> escalateFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> escalationData) {
        
        String reason = escalationData.get("reason");
        String escalatedTo = escalationData.get("escalatedTo");
        CustomerFeedback feedback = customerFeedbackService.escalateFeedback(id, reason, escalatedTo);
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/analytics/rating/average")
    public ResponseEntity<Map<String, Double>> getAverageRating() {
        Double average = customerFeedbackService.getAverageRating();
        return ResponseEntity.ok(Map.of("averageRating", average != null ? average : 0.0));
    }
    
    @GetMapping("/analytics/type/{feedbackType}/count")
    public ResponseEntity<Map<String, Long>> getFeedbackCountByType(@PathVariable String feedbackType) {
        CustomerFeedback.FeedbackType type = CustomerFeedback.FeedbackType.valueOf(feedbackType.toUpperCase());
        Long count = customerFeedbackService.getFeedbackCountByType(type);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @GetMapping("/analytics/rating/{minRating}/count")
    public ResponseEntity<Map<String, Long>> getFeedbackCountByMinRating(@PathVariable Integer minRating) {
        Long count = customerFeedbackService.getFeedbackCountByMinRating(minRating);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.xypay.customer.repository;

import com.xypay.customer.domain.CustomerFeedback;
import com.xypay.customer.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    
    List<CustomerFeedback> findByCustomer(User customer);
    
    List<CustomerFeedback> findByFeedbackType(CustomerFeedback.FeedbackType feedbackType);
    
    List<CustomerFeedback> findByStatus(CustomerFeedback.FeedbackStatus status);
    
    List<CustomerFeedback> findByRating(Integer rating);
    
    List<CustomerFeedback> findByIsPublic(Boolean isPublic);
    
    List<CustomerFeedback> findByEscalated(Boolean escalated);
    
    @Query("SELECT f FROM CustomerFeedback f WHERE f.assignedTo = :assignedTo")
    List<CustomerFeedback> findByAssignedTo(@Param("assignedTo") String assignedTo);
    
    @Query("SELECT f FROM CustomerFeedback f WHERE f.followUpRequired = true AND f.followUpDate <= :now")
    List<CustomerFeedback> findOverdueFollowUps(@Param("now") LocalDateTime now);
    
    @Query("SELECT f FROM CustomerFeedback f WHERE f.createdAt >= :startDate AND f.createdAt <= :endDate")
    List<CustomerFeedback> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(f.rating) FROM CustomerFeedback f WHERE f.rating IS NOT NULL")
    Double getAverageRating();
    
    @Query("SELECT COUNT(f) FROM CustomerFeedback f WHERE f.feedbackType = :feedbackType")
    Long countByFeedbackType(@Param("feedbackType") CustomerFeedback.FeedbackType feedbackType);
    
    @Query("SELECT COUNT(f) FROM CustomerFeedback f WHERE f.rating >= :minRating")
    Long countByMinRating(@Param("minRating") Integer minRating);
}

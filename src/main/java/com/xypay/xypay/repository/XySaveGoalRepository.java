package com.xypay.xypay.repository;

import com.xypay.xypay.domain.XySaveGoal;
import com.xypay.xypay.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface XySaveGoalRepository extends JpaRepository<XySaveGoal, UUID> {
    
    List<XySaveGoal> findByUser(User user);
    
    List<XySaveGoal> findByUserAndIsActiveTrue(User user);
    
    List<XySaveGoal> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);
    
    Optional<XySaveGoal> findByIdAndUser(UUID id, User user);
    
    @Query("SELECT g FROM XySaveGoal g WHERE g.user = :user AND g.isActive = true AND g.currentAmount >= g.targetAmount")
    List<XySaveGoal> findCompletedGoalsByUser(@Param("user") User user);
    
    @Query("SELECT g FROM XySaveGoal g WHERE g.user = :user AND g.isActive = true AND g.currentAmount < g.targetAmount")
    List<XySaveGoal> findActiveIncompleteGoalsByUser(@Param("user") User user);
}

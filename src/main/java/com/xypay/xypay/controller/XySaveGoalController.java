package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.XySaveGoal;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.XySaveGoalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/xysave/goals")
public class XySaveGoalController {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveGoalController.class);
    
    @Autowired
    private XySaveGoalService xySaveGoalService;
    
    /**
     * Get all goals for current user
     */
    @GetMapping
    public ResponseEntity<List<XySaveGoalDTO>> getGoals(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<XySaveGoal> goals = xySaveGoalService.getUserGoals(user);
            List<XySaveGoalDTO> goalDTOs = goals.stream().map(XySaveGoalDTO::new).toList();
            return ResponseEntity.ok(goalDTOs);
        } catch (Exception e) {
            logger.error("Error getting goals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active goals only
     */
    @GetMapping("/active")
    public ResponseEntity<List<XySaveGoalDTO>> getActiveGoals(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<XySaveGoal> goals = xySaveGoalService.getActiveGoals(user);
            List<XySaveGoalDTO> goalDTOs = goals.stream().map(XySaveGoalDTO::new).toList();
            return ResponseEntity.ok(goalDTOs);
        } catch (Exception e) {
            logger.error("Error getting active goals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get completed goals
     */
    @GetMapping("/completed")
    public ResponseEntity<List<XySaveGoalDTO>> getCompletedGoals(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<XySaveGoal> goals = xySaveGoalService.getCompletedGoals(user);
            List<XySaveGoalDTO> goalDTOs = goals.stream().map(XySaveGoalDTO::new).toList();
            return ResponseEntity.ok(goalDTOs);
        } catch (Exception e) {
            logger.error("Error getting completed goals: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get goal by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<XySaveGoalDTO> getGoal(@PathVariable UUID id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var goal = xySaveGoalService.getGoalById(user, id);
            
            if (goal.isPresent()) {
                return ResponseEntity.ok(new XySaveGoalDTO(goal.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting goal by ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create a new goal
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGoal(@Valid @RequestBody XySaveGoalCreateRequestDTO request, 
                                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveGoal goal = xySaveGoalService.createGoal(
                user, request.getName(), request.getTargetAmount(), request.getTargetDate());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Goal created successfully");
            response.put("goal", new XySaveGoalDTO(goal));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating goal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create goal"));
        }
    }
    
    /**
     * Update goal
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateGoal(@PathVariable UUID id,
                                                          @RequestBody XySaveGoalCreateRequestDTO request,
                                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveGoal goal = xySaveGoalService.updateGoal(
                user, id, request.getName(), request.getTargetAmount(), request.getTargetDate());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Goal updated successfully");
            response.put("goal", new XySaveGoalDTO(goal));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating goal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update goal"));
        }
    }
    
    /**
     * Update goal progress
     */
    @PostMapping("/{id}/update-progress")
    public ResponseEntity<Map<String, Object>> updateGoalProgress(@PathVariable UUID id,
                                                                  @Valid @RequestBody XySaveGoalUpdateProgressRequestDTO request,
                                                                  Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveGoal goal = xySaveGoalService.updateGoalProgress(user, id, request.getAmount());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Goal progress updated successfully");
            response.put("goal", new XySaveGoalDTO(goal));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating goal progress: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update goal progress"));
        }
    }
    
    /**
     * Deactivate goal
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateGoal(@PathVariable UUID id, 
                                                              Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveGoal goal = xySaveGoalService.deactivateGoal(user, id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Goal deactivated successfully");
            response.put("goal", new XySaveGoalDTO(goal));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deactivating goal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to deactivate goal"));
        }
    }
    
    /**
     * Delete goal
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteGoal(@PathVariable UUID id, 
                                                          Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            xySaveGoalService.deleteGoal(user, id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Goal deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting goal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete goal"));
        }
    }
    
    /**
     * Get goal statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<XySaveGoalService.GoalStatistics> getGoalStatistics(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveGoalService.GoalStatistics statistics = xySaveGoalService.getGoalStatistics(user);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting goal statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

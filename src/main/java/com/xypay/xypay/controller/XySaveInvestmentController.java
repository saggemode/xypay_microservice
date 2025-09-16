package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.XySaveInvestment;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.XySaveInvestmentService;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/xysave/investments")
public class XySaveInvestmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveInvestmentController.class);
    
    @Autowired
    private XySaveInvestmentService xySaveInvestmentService;
    
    /**
     * Get all investments for current user
     */
    @GetMapping
    public ResponseEntity<List<XySaveInvestmentDTO>> getInvestments(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<XySaveInvestment> investments = xySaveInvestmentService.getUserInvestments(user);
            List<XySaveInvestmentDTO> investmentDTOs = investments.stream().map(XySaveInvestmentDTO::new).toList();
            return ResponseEntity.ok(investmentDTOs);
        } catch (Exception e) {
            logger.error("Error getting investments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get active investments only
     */
    @GetMapping("/active")
    public ResponseEntity<List<XySaveInvestmentDTO>> getActiveInvestments(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<XySaveInvestment> investments = xySaveInvestmentService.getActiveInvestments(user);
            List<XySaveInvestmentDTO> investmentDTOs = investments.stream().map(XySaveInvestmentDTO::new).toList();
            return ResponseEntity.ok(investmentDTOs);
        } catch (Exception e) {
            logger.error("Error getting active investments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get investment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<XySaveInvestmentDTO> getInvestment(@PathVariable UUID id, 
                                                            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<XySaveInvestment> investment = xySaveInvestmentService.getInvestmentById(user, id);
            
            if (investment.isPresent()) {
                return ResponseEntity.ok(new XySaveInvestmentDTO(investment.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting investment by ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create a new investment
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createInvestment(@Valid @RequestBody XySaveInvestmentCreateRequestDTO request, 
                                                                Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveInvestment investment = xySaveInvestmentService.createInvestment(
                user, request.getInvestmentType(), request.getAmountInvested(), 
                request.getExpectedReturnRate(), request.getMaturityDate());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Investment created successfully");
            response.put("investment", new XySaveInvestmentDTO(investment));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating investment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create investment"));
        }
    }
    
    /**
     * Liquidate an investment
     */
    @PostMapping("/{id}/liquidate")
    public ResponseEntity<Map<String, Object>> liquidateInvestment(@PathVariable UUID id, 
                                                                  Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveInvestment investment = xySaveInvestmentService.liquidateInvestment(user, id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Investment liquidated successfully");
            response.put("investment", new XySaveInvestmentDTO(investment));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error liquidating investment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to liquidate investment"));
        }
    }
    
    /**
     * Get investment statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<XySaveInvestmentService.InvestmentStatistics> getInvestmentStatistics(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            XySaveInvestmentService.InvestmentStatistics statistics = xySaveInvestmentService.getInvestmentStatistics(user);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting investment statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get investment types
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getInvestmentTypes() {
        try {
            List<Map<String, String>> types = List.of(
                Map.of("code", "TREASURY_BILLS", "description", "Treasury Bills"),
                Map.of("code", "MUTUAL_FUNDS", "description", "Mutual Funds"),
                Map.of("code", "SHORT_TERM_PLACEMENTS", "description", "Short-term Placements"),
                Map.of("code", "GOVERNMENT_BONDS", "description", "Government Bonds")
            );
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            logger.error("Error getting investment types: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

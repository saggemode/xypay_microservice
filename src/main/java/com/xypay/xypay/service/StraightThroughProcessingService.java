package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.STPRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
public class StraightThroughProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(StraightThroughProcessingService.class);
    
    @Autowired
    private STPRuleRepository stpRuleRepository;
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    @Autowired
    private NotificationService notificationService;
    
    // Removed unused objectMapper field

    /**
     * Process transaction through STP rules
     */
    public STPResult processTransaction(Transaction transaction, User user, Map<String, Object> context) {
        logger.info("Starting STP processing for transaction: {}", transaction.getId());
        
        STPResult result = new STPResult();
        result.setTransactionId((long) transaction.getId().hashCode());
        result.setProcessingStartTime(LocalDateTime.now());
        
        try {
            // Get applicable rules for transactions
            List<STPRule> rules = stpRuleRepository.findApplicableRules("TRANSACTION");
            
            // Evaluate rules in priority order
            for (STPRule rule : rules) {
                STPRuleEvaluation evaluation = evaluateRule(rule, transaction, user, context);
                result.addRuleEvaluation(evaluation);
                
                if (evaluation.isMatched()) {
                    if (rule.getAutoApprove()) {
                        result.setDecision("AUTO_APPROVED");
                        result.setReasonCode("STP_AUTO_APPROVAL");
                        result.setReasonMessage("Transaction auto-approved by STP rule: " + rule.getRuleName());
                        
                        // Execute auto-approval
                        executeAutoApproval(transaction, rule, result);
                        break;
                    } else if (rule.getSkipManualReview()) {
                        result.setDecision("SKIP_REVIEW");
                        result.setReasonCode("STP_SKIP_REVIEW");
                        result.setReasonMessage("Manual review skipped by STP rule: " + rule.getRuleName());
                        break;
                    }
                }
            }
            
            // If no auto-approval rules matched, determine next action
            if (result.getDecision() == null) {
                result.setDecision("REQUIRES_APPROVAL");
                result.setReasonCode("STP_MANUAL_REVIEW");
                result.setReasonMessage("Transaction requires manual approval");
                
                // Start workflow if needed
                startApprovalWorkflow(transaction, user, result);
            }
            
            result.setProcessingEndTime(LocalDateTime.now());
            result.setProcessingTimeMs(
                java.time.Duration.between(result.getProcessingStartTime(), result.getProcessingEndTime()).toMillis()
            );
            
            logger.info("STP processing completed for transaction: {} with decision: {}", 
                transaction.getId(), result.getDecision());
            
            return result;
            
        } catch (Exception e) {
            logger.error("STP processing failed for transaction: {}", transaction.getId(), e);
            result.setDecision("ERROR");
            result.setReasonCode("STP_ERROR");
            result.setReasonMessage("STP processing failed: " + e.getMessage());
            result.setProcessingEndTime(LocalDateTime.now());
            return result;
        }
    }

    /**
     * Evaluate a single STP rule
     */
    private STPRuleEvaluation evaluateRule(STPRule rule, Transaction transaction, User user, Map<String, Object> context) {
        STPRuleEvaluation evaluation = new STPRuleEvaluation();
        evaluation.setRuleId(rule.getId());
        evaluation.setRuleName(rule.getRuleName());
        evaluation.setEvaluationTime(LocalDateTime.now());
        
        try {
            boolean matched = false;
            
            switch (rule.getRuleType()) {
                case "AMOUNT_RANGE":
                    matched = evaluateAmountRange(rule, transaction);
                    break;
                case "CHANNEL_TYPE":
                    matched = evaluateChannelType(rule, transaction);
                    break;
                case "KYC_STATUS":
                    matched = evaluateKycStatus(rule, user);
                    break;
                case "TIME_BASED":
                    matched = evaluateTimeBased(rule);
                    break;
                case "USER_TIER":
                    matched = evaluateUserTier(rule, user);
                    break;
                case "TRANSACTION_TYPE":
                    matched = evaluateTransactionType(rule, transaction);
                    break;
                default:
                    matched = evaluateCustomRule(rule, transaction, user, context);
            }
            
            evaluation.setMatched(matched);
            evaluation.setEvaluationDetails(String.format(
                "Rule type: %s, Condition: %s %s %s, Result: %s",
                rule.getRuleType(),
                rule.getConditionField(),
                rule.getConditionOperator(),
                rule.getConditionValue(),
                matched ? "MATCHED" : "NOT_MATCHED"
            ));
            
        } catch (Exception e) {
            evaluation.setMatched(false);
            evaluation.setEvaluationDetails("Rule evaluation failed: " + e.getMessage());
            logger.error("Rule evaluation failed for rule {}: {}", rule.getId(), e.getMessage());
        }
        
        return evaluation;
    }

    /**
     * Evaluate amount range rule
     */
    private boolean evaluateAmountRange(STPRule rule, Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        
        if (rule.getMinAmount() != null && amount.compareTo(rule.getMinAmount()) < 0) {
            return false;
        }
        
        if (rule.getMaxAmount() != null && amount.compareTo(rule.getMaxAmount()) > 0) {
            return false;
        }
        
        return true;
    }

    /**
     * Evaluate channel type rule
     */
    private boolean evaluateChannelType(STPRule rule, Transaction transaction) {
        String channel = transaction.getChannel();
        String allowedChannels = rule.getAllowedChannels();
        
        if (allowedChannels == null || channel == null) {
            return false;
        }
        
        return Arrays.asList(allowedChannels.split(",")).contains(channel.trim());
    }

    /**
     * Evaluate KYC status rule
     */
    private boolean evaluateKycStatus(STPRule rule, User user) {
        // This would typically check the user's KYC status
        // For now, we'll simulate based on user profile
        String requiredKycLevel = rule.getRequiredKycLevel();
        
        if (requiredKycLevel == null) {
            return true;
        }
        
        // Simulate KYC check - in real implementation, check UserProfile
        return "VERIFIED".equals(requiredKycLevel); // Simplified
    }

    /**
     * Evaluate time-based rule
     */
    private boolean evaluateTimeBased(STPRule rule) {
        LocalTime now = LocalTime.now();
        
        // Example: Only allow auto-approval during business hours (9 AM - 5 PM)
        if ("BUSINESS_HOURS".equals(rule.getConditionValue())) {
            return now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(17, 0));
        }
        
        return true;
    }

    /**
     * Evaluate user tier rule
     */
    private boolean evaluateUserTier(STPRule rule, User user) {
        // This would typically check user tier/category
        // For now, we'll simulate based on user creation date
        String requiredTier = rule.getConditionValue();
        
        if ("PREMIUM".equals(requiredTier)) {
            // Premium users are those registered more than 30 days ago (simplified)
            return user.getCreatedAt().isBefore(LocalDateTime.now().minusDays(30));
        }
        
        return true;
    }

    /**
     * Evaluate transaction type rule
     */
    private boolean evaluateTransactionType(STPRule rule, Transaction transaction) {
        String transactionType = transaction.getType();
        String allowedTypes = rule.getConditionValue();
        
        if (allowedTypes == null || transactionType == null) {
            return false;
        }
        
        return Arrays.asList(allowedTypes.split(",")).contains(transactionType.trim());
    }

    /**
     * Evaluate custom rule
     */
    private boolean evaluateCustomRule(STPRule rule, Transaction transaction, User user, Map<String, Object> context) {
        // Custom rule evaluation logic
        // This can be extended for complex business rules
        return false;
    }

    /**
     * Execute auto-approval
     */
    private void executeAutoApproval(Transaction transaction, STPRule rule, STPResult result) {
        try {
            // Update transaction status
            transaction.setStatus("completed");
            
            // Log the auto-approval
            logger.info("Transaction {} auto-approved by STP rule: {}", 
                transaction.getId(), rule.getRuleName());
            
            // Send notification
            String message = String.format("Transaction %s auto-approved (Amount: %s)", 
                transaction.getReference(), transaction.getAmount());
            
            notificationService.sendNotification(
                transaction.getWallet().getUser().getId(), 
                "TRANSACTION_AUTO_APPROVED", 
                message
            );
            
            result.setAutoApprovalExecuted(true);
            
        } catch (Exception e) {
            logger.error("Auto-approval execution failed: {}", e.getMessage());
            result.setAutoApprovalExecuted(false);
        }
    }

    /**
     * Start approval workflow
     */
    private void startApprovalWorkflow(Transaction transaction, User user, STPResult result) {
        try {
            Map<String, Object> contextData = new HashMap<>();
            contextData.put("transaction_id", transaction.getId());
            contextData.put("amount", transaction.getAmount());
            contextData.put("channel", transaction.getChannel());
            contextData.put("user_id", user.getId());
            
            WorkflowInstance workflow = workflowEngineService.startWorkflow(
                "TRANSACTION_APPROVAL", 
                "TRANSACTION", 
                (long) transaction.getId().hashCode(), 
                (long) user.getId().hashCode(), 
                contextData
            );
            
            result.setWorkflowInstanceId(workflow.getId());
            
        } catch (Exception e) {
            logger.error("Failed to start approval workflow: {}", e.getMessage());
        }
    }

    /**
     * Create default STP rules
     */
    public List<STPRule> createDefaultSTPRules() {
        List<STPRule> rules = new ArrayList<>();
        
        // Auto-approve small amounts
        STPRule smallAmountRule = new STPRule();
        smallAmountRule.setRuleName("Small Amount Auto-Approval");
        smallAmountRule.setRuleType("AMOUNT_RANGE");
        smallAmountRule.setEntityType("TRANSACTION");
        smallAmountRule.setMaxAmount(new BigDecimal("10000"));
        smallAmountRule.setAutoApprove(true);
        smallAmountRule.setPriority(10);
        smallAmountRule.setCreatedBy(1L);
        rules.add(smallAmountRule);
        
        // Auto-approve mobile transactions during business hours
        STPRule mobileBusinessHoursRule = new STPRule();
        mobileBusinessHoursRule.setRuleName("Mobile Business Hours Auto-Approval");
        mobileBusinessHoursRule.setRuleType("TIME_BASED");
        mobileBusinessHoursRule.setEntityType("TRANSACTION");
        mobileBusinessHoursRule.setAllowedChannels("mobile");
        mobileBusinessHoursRule.setConditionValue("BUSINESS_HOURS");
        mobileBusinessHoursRule.setMaxAmount(new BigDecimal("50000"));
        mobileBusinessHoursRule.setAutoApprove(true);
        mobileBusinessHoursRule.setPriority(8);
        mobileBusinessHoursRule.setCreatedBy(1L);
        rules.add(mobileBusinessHoursRule);
        
        // Skip review for verified users with small amounts
        STPRule verifiedUserRule = new STPRule();
        verifiedUserRule.setRuleName("Verified User Skip Review");
        verifiedUserRule.setRuleType("KYC_STATUS");
        verifiedUserRule.setEntityType("TRANSACTION");
        verifiedUserRule.setRequiredKycLevel("VERIFIED");
        verifiedUserRule.setMaxAmount(new BigDecimal("25000"));
        verifiedUserRule.setSkipManualReview(true);
        verifiedUserRule.setPriority(7);
        verifiedUserRule.setCreatedBy(1L);
        rules.add(verifiedUserRule);
        
        // Large amount requires approval
        STPRule largeAmountRule = new STPRule();
        largeAmountRule.setRuleName("Large Amount Manual Review");
        largeAmountRule.setRuleType("AMOUNT_RANGE");
        largeAmountRule.setEntityType("TRANSACTION");
        largeAmountRule.setMinAmount(new BigDecimal("100000"));
        largeAmountRule.setAutoApprove(false);
        largeAmountRule.setSkipManualReview(false);
        largeAmountRule.setPriority(5);
        largeAmountRule.setCreatedBy(1L);
        rules.add(largeAmountRule);
        
        return stpRuleRepository.saveAll(rules);
    }

    /**
     * Get STP statistics
     */
    public Map<String, Object> getSTPStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalRules = stpRuleRepository.count();
        long activeRules = stpRuleRepository.findByEntityTypeAndIsActiveTrueOrderByPriorityDesc("TRANSACTION").size();
        long autoApprovalRules = stpRuleRepository.findAutoApprovalRules("TRANSACTION").size();
        
        stats.put("total_rules", totalRules);
        stats.put("active_rules", activeRules);
        stats.put("auto_approval_rules", autoApprovalRules);
        
        return stats;
    }

    /**
     * STP Result class
     */
    public static class STPResult {
        private Long transactionId;
        private String decision;
        private String reasonCode;
        private String reasonMessage;
        private LocalDateTime processingStartTime;
        private LocalDateTime processingEndTime;
        private Long processingTimeMs;
        private List<STPRuleEvaluation> ruleEvaluations = new ArrayList<>();
        private boolean autoApprovalExecuted = false;
        private Long workflowInstanceId;

        // Getters and setters
        public Long getTransactionId() { return transactionId; }
        public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
        
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        
        public String getReasonCode() { return reasonCode; }
        public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
        
        public String getReasonMessage() { return reasonMessage; }
        public void setReasonMessage(String reasonMessage) { this.reasonMessage = reasonMessage; }
        
        public LocalDateTime getProcessingStartTime() { return processingStartTime; }
        public void setProcessingStartTime(LocalDateTime processingStartTime) { this.processingStartTime = processingStartTime; }
        
        public LocalDateTime getProcessingEndTime() { return processingEndTime; }
        public void setProcessingEndTime(LocalDateTime processingEndTime) { this.processingEndTime = processingEndTime; }
        
        public Long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
        
        public List<STPRuleEvaluation> getRuleEvaluations() { return ruleEvaluations; }
        public void addRuleEvaluation(STPRuleEvaluation evaluation) { this.ruleEvaluations.add(evaluation); }
        
        public boolean isAutoApprovalExecuted() { return autoApprovalExecuted; }
        public void setAutoApprovalExecuted(boolean autoApprovalExecuted) { this.autoApprovalExecuted = autoApprovalExecuted; }
        
        public Long getWorkflowInstanceId() { return workflowInstanceId; }
        public void setWorkflowInstanceId(Long workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }
    }

    /**
     * STP Rule Evaluation class
     */
    public static class STPRuleEvaluation {
        private Long ruleId;
        private String ruleName;
        private boolean matched;
        private String evaluationDetails;
        private LocalDateTime evaluationTime;

        // Getters and setters
        public Long getRuleId() { return ruleId; }
        public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
        
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        
        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
        
        public String getEvaluationDetails() { return evaluationDetails; }
        public void setEvaluationDetails(String evaluationDetails) { this.evaluationDetails = evaluationDetails; }
        
        public LocalDateTime getEvaluationTime() { return evaluationTime; }
        public void setEvaluationTime(LocalDateTime evaluationTime) { this.evaluationTime = evaluationTime; }
    }
}

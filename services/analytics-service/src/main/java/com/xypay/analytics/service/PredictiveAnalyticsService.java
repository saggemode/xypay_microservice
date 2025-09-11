package com.xypay.analytics.service;

import com.xypay.analytics.domain.*;
import com.xypay.analytics.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PredictiveAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(PredictiveAnalyticsService.class);
    
    @Autowired
    private CustomerAnalyticsRepository customerAnalyticsRepository;
    
    @Autowired
    private TransactionAnalyticsRepository transactionAnalyticsRepository;
    
    @Autowired
    private RiskAnalyticsRepository riskAnalyticsRepository;
    
    @Autowired
    private MLModelRepository mlModelRepository;
    
    /**
     * Calculate credit score for customer
     */
    public CreditScore calculateCreditScore(Customer customer) {
        try {
            logger.info("Calculating credit score for customer: {}", customer.getId());
            
            // Get customer analytics
            CustomerAnalytics analytics = customerAnalyticsRepository
                .findByCustomerId(customer.getId())
                .orElse(createCustomerAnalytics(customer));
            
            // Calculate score components
            BigDecimal paymentHistoryScore = calculatePaymentHistoryScore(analytics);
            BigDecimal creditUtilizationScore = calculateCreditUtilizationScore(analytics);
            BigDecimal accountAgeScore = calculateAccountAgeScore(analytics);
            BigDecimal transactionPatternScore = calculateTransactionPatternScore(analytics);
            BigDecimal riskFactorsScore = calculateRiskFactorsScore(analytics);
            
            // Weighted average
            BigDecimal totalScore = paymentHistoryScore.multiply(new BigDecimal("0.35"))
                .add(creditUtilizationScore.multiply(new BigDecimal("0.30")))
                .add(accountAgeScore.multiply(new BigDecimal("0.15")))
                .add(transactionPatternScore.multiply(new BigDecimal("0.15")))
                .add(riskFactorsScore.multiply(new BigDecimal("0.05")));
            
            // Create credit score
            CreditScore creditScore = new CreditScore();
            creditScore.setCustomerId(customer.getId());
            creditScore.setScore(totalScore);
            creditScore.setScoreDate(LocalDateTime.now());
            creditScore.setScoreComponents(createScoreComponents(
                paymentHistoryScore, creditUtilizationScore, accountAgeScore,
                transactionPatternScore, riskFactorsScore));
            
            // Determine risk category
            creditScore.setRiskCategory(determineRiskCategory(totalScore));
            
            // Calculate probability of default
            BigDecimal defaultProbability = calculateDefaultProbability(totalScore);
            creditScore.setDefaultProbability(defaultProbability);
            
            logger.info("Calculated credit score: {} for customer: {}", 
                totalScore, customer.getId());
            
            return creditScore;
            
        } catch (Exception e) {
            logger.error("Error calculating credit score: {}", e.getMessage());
            throw new RuntimeException("Failed to calculate credit score", e);
        }
    }
    
    /**
     * Assess fraud risk for transaction
     */
    public FraudRiskScore assessFraudRisk(Transaction transaction) {
        try {
            logger.info("Assessing fraud risk for transaction: {}", transaction.getId());
            
            // Get transaction analytics
            TransactionAnalytics analytics = transactionAnalyticsRepository
                .findByTransactionId(transaction.getId())
                .orElse(createTransactionAnalytics(transaction));
            
            // Calculate risk factors
            BigDecimal amountRisk = calculateAmountRisk(transaction);
            BigDecimal timeRisk = calculateTimeRisk(transaction);
            BigDecimal locationRisk = calculateLocationRisk(transaction);
            BigDecimal patternRisk = calculatePatternRisk(analytics);
            BigDecimal velocityRisk = calculateVelocityRisk(analytics);
            
            // Calculate total risk score
            BigDecimal totalRisk = amountRisk.multiply(new BigDecimal("0.25"))
                .add(timeRisk.multiply(new BigDecimal("0.20")))
                .add(locationRisk.multiply(new BigDecimal("0.20")))
                .add(patternRisk.multiply(new BigDecimal("0.20")))
                .add(velocityRisk.multiply(new BigDecimal("0.15")));
            
            // Create fraud risk score
            FraudRiskScore fraudScore = new FraudRiskScore();
            fraudScore.setTransactionId(transaction.getId());
            fraudScore.setRiskScore(totalRisk);
            fraudScore.setAssessmentDate(LocalDateTime.now());
            fraudScore.setRiskFactors(createRiskFactors(
                amountRisk, timeRisk, locationRisk, patternRisk, velocityRisk));
            
            // Determine risk level
            fraudScore.setRiskLevel(determineFraudRiskLevel(totalRisk));
            
            // Generate recommendations
            fraudScore.setRecommendations(generateFraudRecommendations(totalRisk));
            
            logger.info("Assessed fraud risk: {} for transaction: {}", 
                totalRisk, transaction.getId());
            
            return fraudScore;
            
        } catch (Exception e) {
            logger.error("Error assessing fraud risk: {}", e.getMessage());
            throw new RuntimeException("Failed to assess fraud risk", e);
        }
    }
    
    /**
     * Segment customer based on behavior and value
     */
    public CustomerSegment segmentCustomer(Customer customer) {
        try {
            logger.info("Segmenting customer: {}", customer.getId());
            
            // Get customer analytics
            CustomerAnalytics analytics = customerAnalyticsRepository
                .findByCustomerId(customer.getId())
                .orElse(createCustomerAnalytics(customer));
            
            // Calculate customer value
            BigDecimal customerValue = calculateCustomerValue(analytics);
            
            // Calculate customer behavior score
            BigDecimal behaviorScore = calculateBehaviorScore(analytics);
            
            // Determine segment
            CustomerSegment segment = determineCustomerSegment(customerValue, behaviorScore);
            
            // Calculate segment metrics
            segment.setCustomerId(customer.getId());
            segment.setSegmentDate(LocalDateTime.now());
            segment.setCustomerValue(customerValue);
            segment.setBehaviorScore(behaviorScore);
            segment.setSegmentConfidence(calculateSegmentConfidence(analytics));
            
            logger.info("Segmented customer: {} as {}", customer.getId(), segment.getSegmentType());
            
            return segment;
            
        } catch (Exception e) {
            logger.error("Error segmenting customer: {}", e.getMessage());
            throw new RuntimeException("Failed to segment customer", e);
        }
    }
    
    /**
     * Generate real-time insights
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void generateRealTimeInsights() {
        try {
            logger.info("Generating real-time insights");
            
            // Transaction volume insights
            generateTransactionVolumeInsights();
            
            // Customer behavior insights
            generateCustomerBehaviorInsights();
            
            // Risk insights
            generateRiskInsights();
            
            // Performance insights
            generatePerformanceInsights();
            
            logger.info("Completed real-time insights generation");
            
        } catch (Exception e) {
            logger.error("Error generating real-time insights: {}", e.getMessage());
        }
    }
    
    /**
     * Calculate payment history score
     */
    private BigDecimal calculatePaymentHistoryScore(CustomerAnalytics analytics) {
        // Simplified calculation based on payment patterns
        BigDecimal onTimePayments = analytics.getOnTimePayments();
        BigDecimal totalPayments = analytics.getTotalPayments();
        
        if (totalPayments.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("500"); // Neutral score for new customers
        }
        
        BigDecimal paymentRatio = onTimePayments.divide(totalPayments, 4, BigDecimal.ROUND_HALF_UP);
        return paymentRatio.multiply(new BigDecimal("850")).add(new BigDecimal("150"));
    }
    
    /**
     * Calculate credit utilization score
     */
    private BigDecimal calculateCreditUtilizationScore(CustomerAnalytics analytics) {
        BigDecimal creditLimit = analytics.getCreditLimit();
        BigDecimal creditUsed = analytics.getCreditUsed();
        
        if (creditLimit.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("750"); // High score for no credit
        }
        
        BigDecimal utilizationRatio = creditUsed.divide(creditLimit, 4, BigDecimal.ROUND_HALF_UP);
        
        // Lower utilization = higher score
        if (utilizationRatio.compareTo(new BigDecimal("0.30")) <= 0) {
            return new BigDecimal("850");
        } else if (utilizationRatio.compareTo(new BigDecimal("0.70")) <= 0) {
            return new BigDecimal("650");
        } else {
            return new BigDecimal("400");
        }
    }
    
    /**
     * Calculate account age score
     */
    private BigDecimal calculateAccountAgeScore(CustomerAnalytics analytics) {
        long accountAgeMonths = analytics.getAccountAgeMonths();
        
        if (accountAgeMonths >= 60) {
            return new BigDecimal("850");
        } else if (accountAgeMonths >= 24) {
            return new BigDecimal("700");
        } else if (accountAgeMonths >= 12) {
            return new BigDecimal("550");
        } else {
            return new BigDecimal("400");
        }
    }
    
    /**
     * Calculate transaction pattern score
     */
    private BigDecimal calculateTransactionPatternScore(CustomerAnalytics analytics) {
        // Analyze transaction patterns for consistency
        BigDecimal patternConsistency = analytics.getPatternConsistency();
        return patternConsistency.multiply(new BigDecimal("800")).add(new BigDecimal("200"));
    }
    
    /**
     * Calculate risk factors score
     */
    private BigDecimal calculateRiskFactorsScore(CustomerAnalytics analytics) {
        // Analyze various risk factors
        BigDecimal riskFactorCount = analytics.getRiskFactorCount();
        
        if (riskFactorCount.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("800");
        } else if (riskFactorCount.compareTo(new BigDecimal("2")) <= 0) {
            return new BigDecimal("600");
        } else {
            return new BigDecimal("300");
        }
    }
    
    /**
     * Determine risk category based on score
     */
    private String determineRiskCategory(BigDecimal score) {
        if (score.compareTo(new BigDecimal("750")) >= 0) {
            return "EXCELLENT";
        } else if (score.compareTo(new BigDecimal("650")) >= 0) {
            return "GOOD";
        } else if (score.compareTo(new BigDecimal("550")) >= 0) {
            return "FAIR";
        } else if (score.compareTo(new BigDecimal("450")) >= 0) {
            return "POOR";
        } else {
            return "VERY_POOR";
        }
    }
    
    /**
     * Calculate default probability
     */
    private BigDecimal calculateDefaultProbability(BigDecimal score) {
        // Inverse relationship: higher score = lower default probability
        BigDecimal maxScore = new BigDecimal("850");
        BigDecimal minProbability = new BigDecimal("0.01"); // 1%
        BigDecimal maxProbability = new BigDecimal("0.30"); // 30%
        
        BigDecimal scoreRatio = score.divide(maxScore, 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal probability = maxProbability.subtract(
            scoreRatio.multiply(maxProbability.subtract(minProbability)));
        
        return probability;
    }
    
    /**
     * Calculate amount risk
     */
    private BigDecimal calculateAmountRisk(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        BigDecimal avgAmount = transactionAnalyticsRepository.getAverageTransactionAmount();
        
        if (amount.compareTo(avgAmount.multiply(new BigDecimal("5"))) > 0) {
            return new BigDecimal("0.8"); // High risk
        } else if (amount.compareTo(avgAmount.multiply(new BigDecimal("2"))) > 0) {
            return new BigDecimal("0.4"); // Medium risk
        } else {
            return new BigDecimal("0.1"); // Low risk
        }
    }
    
    /**
     * Calculate time risk
     */
    private BigDecimal calculateTimeRisk(Transaction transaction) {
        LocalDateTime transactionTime = transaction.getCreatedAt();
        int hour = transactionTime.getHour();
        
        // Transactions outside business hours are riskier
        if (hour >= 22 || hour <= 6) {
            return new BigDecimal("0.6");
        } else if (hour >= 20 || hour <= 8) {
            return new BigDecimal("0.3");
        } else {
            return new BigDecimal("0.1");
        }
    }
    
    /**
     * Calculate location risk
     */
    private BigDecimal calculateLocationRisk(Transaction transaction) {
        // Simplified location risk calculation
        // In real implementation, would check against known locations
        return new BigDecimal("0.2"); // Default medium risk
    }
    
    /**
     * Calculate pattern risk
     */
    private BigDecimal calculatePatternRisk(TransactionAnalytics analytics) {
        // Analyze transaction patterns for anomalies
        BigDecimal patternAnomalyScore = analytics.getPatternAnomalyScore();
        return patternAnomalyScore;
    }
    
    /**
     * Calculate velocity risk
     */
    private BigDecimal calculateVelocityRisk(TransactionAnalytics analytics) {
        // Check transaction velocity (frequency)
        BigDecimal transactionVelocity = analytics.getTransactionVelocity();
        
        if (transactionVelocity.compareTo(new BigDecimal("10")) > 0) {
            return new BigDecimal("0.7"); // High velocity risk
        } else if (transactionVelocity.compareTo(new BigDecimal("5")) > 0) {
            return new BigDecimal("0.4"); // Medium velocity risk
        } else {
            return new BigDecimal("0.1"); // Low velocity risk
        }
    }
    
    /**
     * Determine fraud risk level
     */
    private String determineFraudRiskLevel(BigDecimal riskScore) {
        if (riskScore.compareTo(new BigDecimal("0.7")) >= 0) {
            return "HIGH";
        } else if (riskScore.compareTo(new BigDecimal("0.4")) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Generate fraud recommendations
     */
    private List<String> generateFraudRecommendations(BigDecimal riskScore) {
        List<String> recommendations = new ArrayList<>();
        
        if (riskScore.compareTo(new BigDecimal("0.7")) >= 0) {
            recommendations.add("BLOCK_TRANSACTION");
            recommendations.add("REQUIRE_MANUAL_REVIEW");
            recommendations.add("SEND_FRAUD_ALERT");
        } else if (riskScore.compareTo(new BigDecimal("0.4")) >= 0) {
            recommendations.add("REQUIRE_ADDITIONAL_VERIFICATION");
            recommendations.add("MONITOR_CLOSELY");
        } else {
            recommendations.add("PROCESS_NORMALLY");
        }
        
        return recommendations;
    }
    
    /**
     * Calculate customer value
     */
    private BigDecimal calculateCustomerValue(CustomerAnalytics analytics) {
        BigDecimal totalTransactions = analytics.getTotalTransactionValue();
        BigDecimal accountBalance = analytics.getAverageAccountBalance();
        BigDecimal creditLimit = analytics.getCreditLimit();
        
        return totalTransactions.multiply(new BigDecimal("0.4"))
            .add(accountBalance.multiply(new BigDecimal("0.3")))
            .add(creditLimit.multiply(new BigDecimal("0.3")));
    }
    
    /**
     * Calculate behavior score
     */
    private BigDecimal calculateBehaviorScore(CustomerAnalytics analytics) {
        BigDecimal transactionFrequency = analytics.getTransactionFrequency();
        BigDecimal channelDiversity = analytics.getChannelDiversity();
        BigDecimal productUsage = analytics.getProductUsage();
        
        return transactionFrequency.multiply(new BigDecimal("0.4"))
            .add(channelDiversity.multiply(new BigDecimal("0.3")))
            .add(productUsage.multiply(new BigDecimal("0.3")));
    }
    
    /**
     * Determine customer segment
     */
    private CustomerSegment determineCustomerSegment(BigDecimal customerValue, BigDecimal behaviorScore) {
        CustomerSegment segment = new CustomerSegment();
        
        if (customerValue.compareTo(new BigDecimal("1000000")) >= 0 && 
            behaviorScore.compareTo(new BigDecimal("0.8")) >= 0) {
            segment.setSegmentType("PREMIUM");
        } else if (customerValue.compareTo(new BigDecimal("500000")) >= 0 && 
                   behaviorScore.compareTo(new BigDecimal("0.6")) >= 0) {
            segment.setSegmentType("GOLD");
        } else if (customerValue.compareTo(new BigDecimal("100000")) >= 0 && 
                   behaviorScore.compareTo(new BigDecimal("0.4")) >= 0) {
            segment.setSegmentType("SILVER");
        } else {
            segment.setSegmentType("BRONZE");
        }
        
        return segment;
    }
    
    /**
     * Calculate segment confidence
     */
    private BigDecimal calculateSegmentConfidence(CustomerAnalytics analytics) {
        // Confidence based on data completeness and recency
        BigDecimal dataCompleteness = analytics.getDataCompleteness();
        BigDecimal dataRecency = analytics.getDataRecency();
        
        return dataCompleteness.multiply(new BigDecimal("0.6"))
            .add(dataRecency.multiply(new BigDecimal("0.4")));
    }
    
    /**
     * Generate transaction volume insights
     */
    private void generateTransactionVolumeInsights() {
        // Implementation for real-time transaction volume analysis
        logger.info("Generating transaction volume insights");
    }
    
    /**
     * Generate customer behavior insights
     */
    private void generateCustomerBehaviorInsights() {
        // Implementation for customer behavior analysis
        logger.info("Generating customer behavior insights");
    }
    
    /**
     * Generate risk insights
     */
    private void generateRiskInsights() {
        // Implementation for risk analysis
        logger.info("Generating risk insights");
    }
    
    /**
     * Generate performance insights
     */
    private void generatePerformanceInsights() {
        // Implementation for performance analysis
        logger.info("Generating performance insights");
    }
    
    /**
     * Create customer analytics if not exists
     */
    private CustomerAnalytics createCustomerAnalytics(Customer customer) {
        CustomerAnalytics analytics = new CustomerAnalytics();
        analytics.setCustomerId(customer.getId());
        analytics.setCreatedAt(LocalDateTime.now());
        return customerAnalyticsRepository.save(analytics);
    }
    
    /**
     * Create transaction analytics if not exists
     */
    private TransactionAnalytics createTransactionAnalytics(Transaction transaction) {
        TransactionAnalytics analytics = new TransactionAnalytics();
        analytics.setTransactionId(transaction.getId());
        analytics.setCreatedAt(LocalDateTime.now());
        return transactionAnalyticsRepository.save(analytics);
    }
    
    /**
     * Create score components
     */
    private Map<String, BigDecimal> createScoreComponents(BigDecimal paymentHistory, 
                                                          BigDecimal creditUtilization,
                                                          BigDecimal accountAge,
                                                          BigDecimal transactionPattern,
                                                          BigDecimal riskFactors) {
        Map<String, BigDecimal> components = new HashMap<>();
        components.put("paymentHistory", paymentHistory);
        components.put("creditUtilization", creditUtilization);
        components.put("accountAge", accountAge);
        components.put("transactionPattern", transactionPattern);
        components.put("riskFactors", riskFactors);
        return components;
    }
    
    /**
     * Create risk factors
     */
    private Map<String, BigDecimal> createRiskFactors(BigDecimal amountRisk,
                                                     BigDecimal timeRisk,
                                                     BigDecimal locationRisk,
                                                     BigDecimal patternRisk,
                                                     BigDecimal velocityRisk) {
        Map<String, BigDecimal> factors = new HashMap<>();
        factors.put("amountRisk", amountRisk);
        factors.put("timeRisk", timeRisk);
        factors.put("locationRisk", locationRisk);
        factors.put("patternRisk", patternRisk);
        factors.put("velocityRisk", velocityRisk);
        return factors;
    }
}

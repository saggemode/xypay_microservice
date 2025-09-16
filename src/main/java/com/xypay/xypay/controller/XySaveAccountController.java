package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.*;
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

@RestController
@RequestMapping("/api/xysave/account")
public class XySaveAccountController {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveAccountController.class);
    
    @Autowired
    private XySaveAccountService xySaveAccountService;
    
    @Autowired
    private XySaveTransactionService xySaveTransactionService;
    
    @Autowired
    private XySaveAutoSaveService xySaveAutoSaveService;
    
    @Autowired
    private XySaveInterestService xySaveInterestService;
    
    @Autowired
    private TransactionSecurityService transactionSecurityService;
    
    /**
     * Get or create XySave account for current user
     */
    @GetMapping
    public ResponseEntity<XySaveAccountDTO> getAccount(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var account = xySaveAccountService.getXySaveAccount(user);
            return ResponseEntity.ok(new XySaveAccountDTO(account));
        } catch (Exception e) {
            logger.error("Error getting XySave account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get comprehensive XySave account summary
     */
    @GetMapping("/summary")
    public ResponseEntity<XySaveAccountSummaryDTO> getAccountSummary(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Map<String, Object> summaryData = xySaveAccountService.getAccountSummary(user);
            
            XySaveAccountSummaryDTO summary = new XySaveAccountSummaryDTO();
            summary.setAccount(new XySaveAccountDTO((com.xypay.xypay.domain.XySaveAccount) summaryData.get("account")));
            summary.setSettings(new XySaveSettingsDTO((com.xypay.xypay.domain.XySaveSettings) summaryData.get("settings")));
            summary.setDailyInterest((java.math.BigDecimal) summaryData.get("daily_interest"));
            summary.setAnnualInterestRate((java.math.BigDecimal) summaryData.get("annual_interest_rate"));
            summary.setRecentTransactions(((List<com.xypay.xypay.domain.XySaveTransaction>) summaryData.get("recent_transactions"))
                .stream().map(XySaveTransactionDTO::new).toList());
            summary.setActiveGoals(((List<com.xypay.xypay.domain.XySaveGoal>) summaryData.get("active_goals"))
                .stream().map(XySaveGoalDTO::new).toList());
            summary.setInvestments(((List<com.xypay.xypay.domain.XySaveInvestment>) summaryData.get("investments"))
                .stream().map(XySaveInvestmentDTO::new).toList());
            summary.setTotalInvested((java.math.BigDecimal) summaryData.get("total_invested"));
            summary.setTotalInvestmentValue((java.math.BigDecimal) summaryData.get("total_investment_value"));
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting XySave summary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get XySave dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<XySaveDashboardDTO> getDashboard(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            // Get account summary
            Map<String, Object> summaryData = xySaveAccountService.getAccountSummary(user);
            XySaveAccountSummaryDTO accountSummary = new XySaveAccountSummaryDTO();
            accountSummary.setAccount(new XySaveAccountDTO((com.xypay.xypay.domain.XySaveAccount) summaryData.get("account")));
            accountSummary.setSettings(new XySaveSettingsDTO((com.xypay.xypay.domain.XySaveSettings) summaryData.get("settings")));
            accountSummary.setDailyInterest((java.math.BigDecimal) summaryData.get("daily_interest"));
            accountSummary.setAnnualInterestRate((java.math.BigDecimal) summaryData.get("annual_interest_rate"));
            accountSummary.setRecentTransactions(((List<com.xypay.xypay.domain.XySaveTransaction>) summaryData.get("recent_transactions"))
                .stream().map(XySaveTransactionDTO::new).toList());
            accountSummary.setActiveGoals(((List<com.xypay.xypay.domain.XySaveGoal>) summaryData.get("active_goals"))
                .stream().map(XySaveGoalDTO::new).toList());
            accountSummary.setInvestments(((List<com.xypay.xypay.domain.XySaveInvestment>) summaryData.get("investments"))
                .stream().map(XySaveInvestmentDTO::new).toList());
            accountSummary.setTotalInvested((java.math.BigDecimal) summaryData.get("total_invested"));
            accountSummary.setTotalInvestmentValue((java.math.BigDecimal) summaryData.get("total_investment_value"));
            
            // Get interest forecast
            Map<String, Object> interestForecast = xySaveInterestService.getInterestForecast(user, 30);
            XySaveInterestForecastDTO interestForecastDTO = new XySaveInterestForecastDTO();
            interestForecastDTO.setDailyInterest((java.math.BigDecimal) interestForecast.get("daily_interest"));
            interestForecastDTO.setWeeklyInterest((java.math.BigDecimal) interestForecast.get("weekly_interest"));
            interestForecastDTO.setMonthlyInterest((java.math.BigDecimal) interestForecast.get("monthly_interest"));
            interestForecastDTO.setYearlyInterest((java.math.BigDecimal) interestForecast.get("yearly_interest"));
            interestForecastDTO.setAnnualRate((java.math.BigDecimal) interestForecast.get("annual_rate"));
            interestForecastDTO.setCurrentBalance((java.math.BigDecimal) interestForecast.get("current_balance"));
            interestForecastDTO.setTotalInterestEarned((java.math.BigDecimal) interestForecast.get("total_interest_earned"));
            
            // Get recent transactions
            var account = (com.xypay.xypay.domain.XySaveAccount) summaryData.get("account");
            List<XySaveTransactionDTO> recentActivity = account.getTransactions() != null ? 
                account.getTransactions().stream()
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .limit(10)
                    .map(XySaveTransactionDTO::new)
                    .toList() : List.of();
            
            // Get active goals
            List<XySaveGoalDTO> goalsProgress = ((List<com.xypay.xypay.domain.XySaveGoal>) summaryData.get("active_goals"))
                .stream().map(XySaveGoalDTO::new).toList();
            
            // Get investments
            List<XySaveInvestmentDTO> investmentPortfolio = ((List<com.xypay.xypay.domain.XySaveInvestment>) summaryData.get("investments"))
                .stream().map(XySaveInvestmentDTO::new).toList();
            
            XySaveDashboardDTO dashboard = new XySaveDashboardDTO();
            dashboard.setAccountSummary(accountSummary);
            dashboard.setInterestForecast(interestForecastDTO);
            dashboard.setRecentActivity(recentActivity);
            dashboard.setGoalsProgress(goalsProgress);
            dashboard.setInvestmentPortfolio(investmentPortfolio);
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Error getting XySave dashboard: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Deposit money to XySave account
     */
    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@Valid @RequestBody XySaveDepositRequestDTO request, 
                                                      Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var transaction = xySaveTransactionService.depositToXySave(
                user, request.getAmount(), request.getDescription());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully deposited ₦" + request.getAmount() + " to XySave");
            response.put("transaction", new XySaveTransactionDTO(transaction));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error depositing to XySave: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process deposit"));
        }
    }
    
    /**
     * Withdraw money from XySave account
     */
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@Valid @RequestBody XySaveWithdrawalRequestDTO request, 
                                                       Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var transaction = xySaveTransactionService.withdrawFromXySave(
                user, request.getAmount(), request.getDescription());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully withdrew ₦" + request.getAmount() + " from XySave");
            response.put("transaction", new XySaveTransactionDTO(transaction));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error withdrawing from XySave: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process withdrawal"));
        }
    }
    
    /**
     * Get interest forecast for XySave account
     */
    @GetMapping("/interest-forecast")
    public ResponseEntity<XySaveInterestForecastDTO> getInterestForecast(
            @RequestParam(defaultValue = "30") int days, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Map<String, Object> forecast = xySaveInterestService.getInterestForecast(user, days);
            
            XySaveInterestForecastDTO forecastDTO = new XySaveInterestForecastDTO();
            forecastDTO.setDailyInterest((java.math.BigDecimal) forecast.get("daily_interest"));
            forecastDTO.setWeeklyInterest((java.math.BigDecimal) forecast.get("weekly_interest"));
            forecastDTO.setMonthlyInterest((java.math.BigDecimal) forecast.get("monthly_interest"));
            forecastDTO.setYearlyInterest((java.math.BigDecimal) forecast.get("yearly_interest"));
            forecastDTO.setAnnualRate((java.math.BigDecimal) forecast.get("annual_rate"));
            forecastDTO.setCurrentBalance((java.math.BigDecimal) forecast.get("current_balance"));
            forecastDTO.setTotalInterestEarned((java.math.BigDecimal) forecast.get("total_interest_earned"));
            
            return ResponseEntity.ok(forecastDTO);
        } catch (Exception e) {
            logger.error("Error getting interest forecast: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get basic security analysis for recent transactions
     */
    @GetMapping("/security-analysis")
    public ResponseEntity<XySaveSecurityAnalysisDTO> getTransactionSecurity(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<Map<String, Object>> securityAnalysis = transactionSecurityService
                .getSecurityAnalysisForRecentTransactions(user, 10);
            
            XySaveSecurityAnalysisDTO analysisDTO = new XySaveSecurityAnalysisDTO();
            analysisDTO.setSecurityAnalysis(securityAnalysis.stream()
                .map(data -> {
                    XySaveSecurityAnalysisDTO.TransactionSecurityAnalysis analysis = 
                        new XySaveSecurityAnalysisDTO.TransactionSecurityAnalysis();
                    analysis.setTransactionId((java.util.UUID) data.get("transaction_id"));
                    analysis.setReference((String) data.get("reference"));
                    analysis.setAmount((java.math.BigDecimal) data.get("amount"));
                    analysis.setRiskLevel((String) data.get("risk_level"));
                    analysis.setRiskFactors((List<String>) data.get("risk_factors"));
                    analysis.setCreatedAt((java.time.LocalDateTime) data.get("created_at"));
                    return analysis;
                })
                .toList());
            analysisDTO.setTotalTransactionsAnalyzed(securityAnalysis.size());
            analysisDTO.setAnalysisTimestamp(java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(analysisDTO);
        } catch (Exception e) {
            logger.error("Error getting security analysis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

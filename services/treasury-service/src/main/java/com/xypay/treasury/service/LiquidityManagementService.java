package com.xypay.treasury.service;

import com.xypay.treasury.domain.*;
import com.xypay.treasury.dto.LiquidityForecastRequest;
import com.xypay.treasury.dto.LiquidityForecastResponse;
import com.xypay.treasury.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LiquidityManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(LiquidityManagementService.class);
    
    @Autowired
    private TreasuryPositionRepository treasuryPositionRepository;
    
    @Autowired
    private LiquidityForecastRepository liquidityForecastRepository;
    
    @Autowired
    private RiskMetricsRepository riskMetricsRepository;
    
    /**
     * Generate liquidity forecast from request
     */
    public LiquidityForecastResponse generateForecast(LiquidityForecastRequest request) {
        LiquidityForecast forecast = generateForecast(request.getCurrencyCode(), request.getForecastDays());
        return LiquidityForecastResponse.fromLiquidityForecast(forecast);
    }
    
    /**
     * Generate liquidity forecast for specified currency and days
     */
    public LiquidityForecast generateForecast(String currencyCode, int forecastDays) {
        try {
            logger.info("Generating liquidity forecast for currency: {}, days: {}", currencyCode, forecastDays);
            
            // Get current positions
            List<TreasuryPosition> positions = treasuryPositionRepository
                .findByCurrencyCodeAndIsActiveTrue(currencyCode);
            
            // Calculate base liquidity
            BigDecimal currentLiquidity = positions.stream()
                .filter(p -> p.getLiquidityBucket() == TreasuryPosition.LiquidityBucket.INSTANT)
                .map(TreasuryPosition::getAvailableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Generate forecast
            LiquidityForecast forecast = new LiquidityForecast();
            forecast.setCurrencyCode(currencyCode);
            forecast.setForecastDate(LocalDate.now());
            forecast.setForecastDays(forecastDays);
            forecast.setCurrentLiquidity(currentLiquidity);
            
            // Calculate projected liquidity by day
            for (int day = 1; day <= forecastDays; day++) {
                LocalDate forecastDate = LocalDate.now().plusDays(day);
                BigDecimal projectedLiquidity = calculateProjectedLiquidity(positions, forecastDate);
                
                LiquidityProjection projection = new LiquidityProjection();
                projection.setForecastDate(forecastDate);
                projection.setProjectedLiquidity(projectedLiquidity);
                projection.setConfidenceLevel(calculateConfidenceLevel(day));
                
                forecast.addProjection(projection);
            }
            
            // Calculate risk metrics
            RiskMetrics riskMetrics = calculateRiskMetrics(forecast);
            forecast.setRiskMetrics(riskMetrics);
            
            LiquidityForecast saved = liquidityForecastRepository.save(forecast);
            
            logger.info("Generated liquidity forecast {} for currency: {}", 
                saved.getId(), currencyCode);
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error generating liquidity forecast: {}", e.getMessage());
            throw new RuntimeException("Failed to generate liquidity forecast", e);
        }
    }
    
    /**
     * Rebalance treasury positions based on liquidity requirements
     */
    public void rebalancePositions() {
        try {
            logger.info("Starting treasury position rebalancing");
            
            // Get all active positions
            List<TreasuryPosition> positions = treasuryPositionRepository.findByIsActiveTrue();
            
            // Group by currency
            Map<String, List<TreasuryPosition>> positionsByCurrency = positions.stream()
                .collect(Collectors.groupingBy(TreasuryPosition::getCurrencyCode));
            
            for (Map.Entry<String, List<TreasuryPosition>> entry : positionsByCurrency.entrySet()) {
                String currency = entry.getKey();
                List<TreasuryPosition> currencyPositions = entry.getValue();
                
                // Generate forecast
                LiquidityForecast forecast = generateForecast(currency, 30);
                
                // Check if rebalancing is needed
                if (needsRebalancing(forecast)) {
                    executeRebalancing(currencyPositions, forecast);
                }
            }
            
            logger.info("Completed treasury position rebalancing");
            
        } catch (Exception e) {
            logger.error("Error in treasury position rebalancing: {}", e.getMessage());
        }
    }
    
    /**
     * Calculate risk metrics for treasury operations
     */
    public RiskMetrics calculateRiskMetrics(LiquidityForecast forecast) {
        RiskMetrics metrics = new RiskMetrics();
        
        // Calculate liquidity risk
        BigDecimal minLiquidity = forecast.getProjections().stream()
            .map(LiquidityProjection::getProjectedLiquidity)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        metrics.setLiquidityRisk(minLiquidity);
        
        // Calculate concentration risk
        BigDecimal totalExposure = forecast.getCurrentLiquidity();
        BigDecimal concentrationRisk = totalExposure.multiply(new BigDecimal("0.1")); // 10% threshold
        metrics.setConcentrationRisk(concentrationRisk);
        
        // Calculate market risk
        BigDecimal marketRisk = calculateMarketRisk(forecast);
        metrics.setMarketRisk(marketRisk);
        
        // Calculate operational risk
        BigDecimal operationalRisk = new BigDecimal("1000000"); // Fixed operational risk limit
        metrics.setOperationalRisk(operationalRisk);
        
        // Calculate total risk
        BigDecimal totalRisk = metrics.getLiquidityRisk()
            .add(metrics.getConcentrationRisk())
            .add(metrics.getMarketRisk())
            .add(metrics.getOperationalRisk());
        metrics.setTotalRisk(totalRisk);
        
        return riskMetricsRepository.save(metrics);
    }
    
    /**
     * Daily liquidity monitoring (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void monitorLiquidity() {
        try {
            logger.info("Starting liquidity monitoring");
            
            // Get all currencies
            List<String> currencies = treasuryPositionRepository.findDistinctCurrencyCodes();
            
            for (String currency : currencies) {
                LiquidityForecast forecast = generateForecast(currency, 7); // 7-day forecast
                
                // Check for liquidity alerts
                checkLiquidityAlerts(forecast);
            }
            
            logger.info("Completed liquidity monitoring");
            
        } catch (Exception e) {
            logger.error("Error in liquidity monitoring: {}", e.getMessage());
        }
    }
    
    /**
     * Calculate projected liquidity for a specific date
     */
    private BigDecimal calculateProjectedLiquidity(List<TreasuryPosition> positions, LocalDate forecastDate) {
        return positions.stream()
            .filter(p -> p.getMaturityDate() == null || p.getMaturityDate().isAfter(forecastDate))
            .map(TreasuryPosition::getAvailableAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate confidence level for forecast
     */
    private BigDecimal calculateConfidenceLevel(int daysAhead) {
        // Confidence decreases with time
        if (daysAhead <= 1) return new BigDecimal("0.95");
        if (daysAhead <= 7) return new BigDecimal("0.85");
        if (daysAhead <= 30) return new BigDecimal("0.70");
        return new BigDecimal("0.50");
    }
    
    /**
     * Check if rebalancing is needed
     */
    private boolean needsRebalancing(LiquidityForecast forecast) {
        BigDecimal minRequiredLiquidity = new BigDecimal("10000000"); // ₦10M minimum
        
        return forecast.getProjections().stream()
            .anyMatch(p -> p.getProjectedLiquidity().compareTo(minRequiredLiquidity) < 0);
    }
    
    /**
     * Execute rebalancing operations
     */
    private void executeRebalancing(List<TreasuryPosition> positions, LiquidityForecast forecast) {
        logger.info("Executing rebalancing for currency: {}", 
            positions.get(0).getCurrencyCode());
        
        // Implementation would include:
        // 1. Moving funds from longer-term to shorter-term positions
        // 2. Calling external liquidity providers
        // 3. Adjusting investment allocations
        // 4. Creating new positions as needed
    }
    
    /**
     * Calculate market risk
     */
    private BigDecimal calculateMarketRisk(LiquidityForecast forecast) {
        // Simplified market risk calculation
        return forecast.getCurrentLiquidity().multiply(new BigDecimal("0.05")); // 5% market risk
    }
    
    /**
     * Check for liquidity alerts
     */
    private void checkLiquidityAlerts(LiquidityForecast forecast) {
        BigDecimal alertThreshold = new BigDecimal("5000000"); // ₦5M alert threshold
        
        forecast.getProjections().stream()
            .filter(p -> p.getProjectedLiquidity().compareTo(alertThreshold) < 0)
            .forEach(p -> {
                logger.warn("Liquidity alert: Currency {}, Date {}, Liquidity {}", 
                    forecast.getCurrencyCode(), p.getForecastDate(), p.getProjectedLiquidity());
                
                // Send alert notification
                sendLiquidityAlert(forecast.getCurrencyCode(), p.getForecastDate(), p.getProjectedLiquidity());
            });
    }
    
    /**
     * Send liquidity alert notification
     */
    private void sendLiquidityAlert(String currency, LocalDate date, BigDecimal liquidity) {
        // Implementation would send alerts to treasury team
        logger.info("Sending liquidity alert for currency: {}, date: {}, liquidity: {}", 
            currency, date, liquidity);
    }
}

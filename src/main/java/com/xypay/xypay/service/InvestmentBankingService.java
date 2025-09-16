package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
@Transactional
public class InvestmentBankingService {

    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private SecurityRepository securityRepository;
    
    @Autowired
    private SecurityHoldingRepository securityHoldingRepository;
    
    @Autowired
    private SecurityTransactionRepository securityTransactionRepository;
    
    @Autowired
    private BankRepository bankRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Removed unused services to fix warnings

    // Portfolio Management
    public Portfolio createPortfolio(UUID bankId, UUID customerId, String portfolioName, 
                                   Portfolio.PortfolioType portfolioType, 
                                   Portfolio.InvestmentObjective objective,
                                   Portfolio.RiskProfile riskProfile, String baseCurrency) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        User customer = null;
        if (customerId != null) {
            customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        }
        
        Portfolio portfolio = new Portfolio();
        portfolio.setBank(bank);
        portfolio.setCustomer(customer);
        portfolio.setPortfolioNumber(generatePortfolioNumber());
        portfolio.setPortfolioName(portfolioName);
        portfolio.setPortfolioType(portfolioType);
        portfolio.setInvestmentObjective(objective);
        portfolio.setRiskProfile(riskProfile);
        portfolio.setBaseCurrency(baseCurrency);
        portfolio.setInceptionDate(LocalDateTime.now());
        portfolio.setStatus(Portfolio.PortfolioStatus.ACTIVE);
        portfolio.setIfrsClassification(Portfolio.IfrsClassification.FAIR_VALUE_PL);
        
        // Set default limits based on risk profile
        setDefaultLimits(portfolio, riskProfile);
        
        return portfolioRepository.save(portfolio);
    }

    // Security Management
    public Security createSecurity(String symbol, String securityName, Security.SecurityType securityType,
                                 Security.AssetClass assetClass, String currencyCode, String exchange) {
        
        Security security = new Security();
        security.setSymbol(symbol);
        security.setSecurityName(securityName);
        security.setSecurityType(securityType);
        security.setAssetClass(assetClass);
        security.setCurrencyCode(currencyCode);
        security.setExchange(exchange);
        security.setIsActive(true);
        security.setIsTradeable(true);
        security.setIsLiquid(true);
        security.setIfrsClassification(Security.IfrsClassification.FAIR_VALUE_PL);
        
        // Set default risk metrics
        security.setBeta(BigDecimal.ONE);
        security.setVolatility(new BigDecimal("0.20")); // 20% volatility
        security.setBaselRiskWeight(new BigDecimal("100")); // 100% risk weight
        
        return securityRepository.save(security);
    }

    public Security updateSecurityPrice(UUID securityId, BigDecimal newPrice, BigDecimal volume) {
        Security security = securityRepository.findById(securityId)
            .orElseThrow(() -> new RuntimeException("Security not found"));
        
        security.setPreviousClose(security.getCurrentPrice());
        security.setCurrentPrice(newPrice);
        security.setVolume(volume);
        security.setLastPriceUpdate(LocalDateTime.now());
        
        // Update day high/low
        if (security.getDayHigh() == null || newPrice.compareTo(security.getDayHigh()) > 0) {
            security.setDayHigh(newPrice);
        }
        if (security.getDayLow() == null || newPrice.compareTo(security.getDayLow()) < 0) {
            security.setDayLow(newPrice);
        }
        
        security = securityRepository.save(security);
        
        // Update all holdings of this security
        updateHoldingsMarketValue(security);
        
        return security;
    }

    // Trading Operations
    public SecurityTransaction executeBuyOrder(UUID portfolioId, UUID securityId, BigDecimal quantity, 
                                             BigDecimal price, SecurityTransaction.OrderType orderType) {
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        Security security = securityRepository.findById(securityId)
            .orElseThrow(() -> new RuntimeException("Security not found"));
        
        // Pre-trade compliance checks
        if (!performPreTradeChecks(portfolio, security, quantity, price, SecurityTransaction.TransactionType.BUY)) {
            throw new RuntimeException("Pre-trade compliance checks failed");
        }
        
        SecurityTransaction transaction = new SecurityTransaction();
        transaction.setPortfolio(portfolio);
        transaction.setSecurity(security);
        transaction.setTransactionNumber(generateTransactionNumber("BUY"));
        transaction.setTransactionType(SecurityTransaction.TransactionType.BUY);
        transaction.setOrderType(orderType);
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setGrossAmount(quantity.multiply(price));
        transaction.setCurrencyCode(security.getCurrencyCode());
        transaction.setTradeDate(LocalDateTime.now());
        transaction.setSettlementDate(LocalDateTime.now().plusDays(2)); // T+2
        transaction.setValueDate(transaction.getSettlementDate());
        transaction.setStatus(SecurityTransaction.TransactionStatus.FILLED);
        
        // Calculate fees and commissions
        calculateTransactionCosts(transaction);
        
        transaction = securityTransactionRepository.save(transaction);
        
        // Update or create holding
        updateHolding(portfolio, security, quantity, price, SecurityTransaction.TransactionType.BUY);
        
        // Update portfolio cash
        updatePortfolioCash(portfolio, transaction.getNetAmount().negate());
        
        return transaction;
    }

    public SecurityTransaction executeSellOrder(UUID portfolioId, UUID securityId, BigDecimal quantity, 
                                              BigDecimal price, SecurityTransaction.OrderType orderType) {
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        Security security = securityRepository.findById(securityId)
            .orElseThrow(() -> new RuntimeException("Security not found"));
        
        // Check if sufficient holdings exist
        SecurityHolding holding = securityHoldingRepository.findByPortfolioAndSecurity(portfolio, security)
            .orElseThrow(() -> new RuntimeException("No holding found for this security"));
        
        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException("Insufficient holdings to sell");
        }
        
        SecurityTransaction transaction = new SecurityTransaction();
        transaction.setPortfolio(portfolio);
        transaction.setSecurity(security);
        transaction.setTransactionNumber(generateTransactionNumber("SELL"));
        transaction.setTransactionType(SecurityTransaction.TransactionType.SELL);
        transaction.setOrderType(orderType);
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setGrossAmount(quantity.multiply(price));
        transaction.setCurrencyCode(security.getCurrencyCode());
        transaction.setTradeDate(LocalDateTime.now());
        transaction.setSettlementDate(LocalDateTime.now().plusDays(2));
        transaction.setValueDate(transaction.getSettlementDate());
        transaction.setStatus(SecurityTransaction.TransactionStatus.FILLED);
        transaction.setCostBasis(holding.getAverageCost());
        
        // Calculate P&L
        BigDecimal realizedPnl = quantity.multiply(price.subtract(holding.getAverageCost()));
        transaction.setRealizedPnl(realizedPnl);
        
        // Calculate fees and commissions
        calculateTransactionCosts(transaction);
        
        transaction = securityTransactionRepository.save(transaction);
        
        // Update holding
        updateHolding(portfolio, security, quantity.negate(), price, SecurityTransaction.TransactionType.SELL);
        
        // Update portfolio cash
        updatePortfolioCash(portfolio, transaction.getNetAmount());
        
        return transaction;
    }

    // Portfolio Valuation
    public void performPortfolioValuation(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        List<SecurityHolding> holdings = securityHoldingRepository.findByPortfolio(portfolio);
        
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalUnrealizedPnl = BigDecimal.ZERO;
        BigDecimal totalAccruedIncome = BigDecimal.ZERO;
        
        for (SecurityHolding holding : holdings) {
            Security security = holding.getSecurity();
            
            if (security.getCurrentPrice() != null) {
                BigDecimal marketValue = holding.getQuantity().multiply(security.getCurrentPrice());
                BigDecimal unrealizedPnl = marketValue.subtract(holding.getTotalCost());
                
                holding.setMarketValue(marketValue);
                holding.setUnrealizedPnl(unrealizedPnl);
                holding.setLastValuationDate(LocalDateTime.now());
                
                // Calculate weight in portfolio
                if (portfolio.getTotalMarketValue().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal weight = marketValue.divide(portfolio.getTotalMarketValue(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                    holding.setWeightInPortfolio(weight);
                }
                
                securityHoldingRepository.save(holding);
                
                totalMarketValue = totalMarketValue.add(marketValue);
                totalCost = totalCost.add(holding.getTotalCost());
                totalUnrealizedPnl = totalUnrealizedPnl.add(unrealizedPnl);
                totalAccruedIncome = totalAccruedIncome.add(holding.getAccruedIncome());
            }
        }
        
        // Update portfolio totals
        portfolio.setTotalMarketValue(totalMarketValue.add(portfolio.getCashBalance()));
        portfolio.setTotalCost(totalCost);
        portfolio.setUnrealizedPnl(totalUnrealizedPnl);
        portfolio.setAccruedIncome(totalAccruedIncome);
        portfolio.setLastValuationDate(LocalDateTime.now());
        
        // Calculate performance metrics
        calculatePerformanceMetrics(portfolio);
        
        // Calculate risk metrics
        calculatePortfolioRiskMetrics(portfolio);
        
        // Update asset allocation
        updateAssetAllocation(portfolio);
        
        portfolioRepository.save(portfolio);
    }

    // Risk Management
    private boolean performPreTradeChecks(Portfolio portfolio, Security security, BigDecimal quantity, 
                                        BigDecimal price, SecurityTransaction.TransactionType transactionType) {
        
        BigDecimal transactionValue = quantity.multiply(price);
        BigDecimal portfolioValue = portfolio.getTotalMarketValue();
        
        if (portfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return true; // First transaction
        }
        
        // Check position size limit
        BigDecimal positionWeight = transactionValue.divide(portfolioValue, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        
        if (positionWeight.compareTo(portfolio.getMaxPositionSize()) > 0) {
            return false;
        }
        
        // Check single issuer limit
        if (security.getIssuer() != null) {
            BigDecimal issuerExposure = calculateIssuerExposure(portfolio, security.getIssuer());
            BigDecimal newExposure = issuerExposure.add(positionWeight);
            
            if (newExposure.compareTo(portfolio.getMaxSingleIssuer()) > 0) {
                return false;
            }
        }
        
        // Check cash availability for buy orders
        if (transactionType == SecurityTransaction.TransactionType.BUY) {
            if (portfolio.getCashBalance().compareTo(transactionValue) < 0) {
                return false;
            }
        }
        
        return true;
    }

    private void calculateTransactionCosts(SecurityTransaction transaction) {
        BigDecimal grossAmount = transaction.getGrossAmount();
        
        // Calculate commission (0.1% of gross amount)
        BigDecimal commission = grossAmount.multiply(new BigDecimal("0.001"));
        transaction.setCommission(commission);
        
        // Calculate fees (fixed fee)
        BigDecimal fees = new BigDecimal("10.00");
        transaction.setFees(fees);
        
        // Calculate taxes (0.1% for sell orders)
        BigDecimal taxes = BigDecimal.ZERO;
        if (transaction.getTransactionType() == SecurityTransaction.TransactionType.SELL) {
            taxes = grossAmount.multiply(new BigDecimal("0.001"));
        }
        transaction.setTaxes(taxes);
        
        // Calculate net amount
        BigDecimal totalCosts = commission.add(fees).add(taxes);
        if (transaction.getTransactionType() == SecurityTransaction.TransactionType.BUY) {
            transaction.setNetAmount(grossAmount.add(totalCosts));
        } else {
            transaction.setNetAmount(grossAmount.subtract(totalCosts));
        }
    }

    private void updateHolding(Portfolio portfolio, Security security, BigDecimal quantity, 
                             BigDecimal price, SecurityTransaction.TransactionType transactionType) {
        
        SecurityHolding holding = securityHoldingRepository.findByPortfolioAndSecurity(portfolio, security)
            .orElse(null);
        
        if (holding == null) {
            // Create new holding
            holding = new SecurityHolding();
            holding.setPortfolio(portfolio);
            holding.setSecurity(security);
            holding.setQuantity(quantity);
            holding.setAverageCost(price);
            holding.setTotalCost(quantity.multiply(price));
            holding.setAcquisitionDate(LocalDateTime.now());
        } else {
            if (transactionType == SecurityTransaction.TransactionType.BUY) {
                // Update existing holding for buy
                BigDecimal newQuantity = holding.getQuantity().add(quantity);
                BigDecimal newTotalCost = holding.getTotalCost().add(quantity.multiply(price));
                BigDecimal newAverageCost = newTotalCost.divide(newQuantity, 6, RoundingMode.HALF_UP);
                
                holding.setQuantity(newQuantity);
                holding.setAverageCost(newAverageCost);
                holding.setTotalCost(newTotalCost);
            } else {
                // Update existing holding for sell
                BigDecimal newQuantity = holding.getQuantity().subtract(quantity);
                BigDecimal soldCost = quantity.multiply(holding.getAverageCost());
                BigDecimal newTotalCost = holding.getTotalCost().subtract(soldCost);
                
                holding.setQuantity(newQuantity);
                holding.setTotalCost(newTotalCost);
                
                // Update realized P&L
                BigDecimal realizedPnl = quantity.multiply(price.subtract(holding.getAverageCost()));
                holding.setRealizedPnl(holding.getRealizedPnl().add(realizedPnl));
            }
        }
        
        // Calculate holding period
        if (holding.getAcquisitionDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                holding.getAcquisitionDate().toLocalDate(), 
                LocalDateTime.now().toLocalDate());
            holding.setHoldingPeriodDays((int) days);
        }
        
        securityHoldingRepository.save(holding);
    }

    private void updateHoldingsMarketValue(Security security) {
        List<SecurityHolding> holdings = securityHoldingRepository.findBySecurity(security);
        
        for (SecurityHolding holding : holdings) {
            if (security.getCurrentPrice() != null) {
                BigDecimal marketValue = holding.getQuantity().multiply(security.getCurrentPrice());
                BigDecimal unrealizedPnl = marketValue.subtract(holding.getTotalCost());
                
                holding.setMarketValue(marketValue);
                holding.setUnrealizedPnl(unrealizedPnl);
                holding.setLastValuationDate(LocalDateTime.now());
                
                securityHoldingRepository.save(holding);
            }
        }
    }

    private void updatePortfolioCash(Portfolio portfolio, BigDecimal amount) {
        portfolio.setCashBalance(portfolio.getCashBalance().add(amount));
        portfolioRepository.save(portfolio);
    }

    private void calculatePerformanceMetrics(Portfolio portfolio) {
        if (portfolio.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
            // Total return
            BigDecimal totalReturn = portfolio.getTotalMarketValue()
                .subtract(portfolio.getTotalCost())
                .divide(portfolio.getTotalCost(), 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            portfolio.setTotalReturn(totalReturn);
            
            // Annualized return (simplified)
            if (portfolio.getInceptionDate() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                    portfolio.getInceptionDate().toLocalDate(), 
                    LocalDateTime.now().toLocalDate());
                
                if (days > 0) {
                    BigDecimal years = new BigDecimal(days).divide(new BigDecimal("365"), 6, RoundingMode.HALF_UP);
                    BigDecimal annualizedReturn = totalReturn.divide(years, 6, RoundingMode.HALF_UP);
                    portfolio.setAnnualizedReturn(annualizedReturn);
                }
            }
        }
    }

    private void calculatePortfolioRiskMetrics(Portfolio portfolio) {
        List<SecurityHolding> holdings = securityHoldingRepository.findByPortfolio(portfolio);
        
        BigDecimal portfolioVar = BigDecimal.ZERO;
        BigDecimal portfolioBeta = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (SecurityHolding holding : holdings) {
            if (holding.getWeightInPortfolio() != null && holding.getSecurity().getBeta() != null) {
                BigDecimal weight = holding.getWeightInPortfolio().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                portfolioBeta = portfolioBeta.add(weight.multiply(holding.getSecurity().getBeta()));
                totalWeight = totalWeight.add(weight);
                
                // Simple VaR calculation
                if (holding.getSecurity().getVar1Day() != null) {
                    portfolioVar = portfolioVar.add(holding.getMarketValue().multiply(new BigDecimal("0.01")));
                }
            }
        }
        
        portfolio.setBeta(portfolioBeta);
        portfolio.setVar1Day(portfolioVar);
        portfolio.setVar10Day(portfolioVar.multiply(new BigDecimal("3.16"))); // sqrt(10)
    }

    private void updateAssetAllocation(Portfolio portfolio) {
        List<SecurityHolding> holdings = securityHoldingRepository.findByPortfolio(portfolio);
        
        BigDecimal equityValue = BigDecimal.ZERO;
        BigDecimal fixedIncomeValue = BigDecimal.ZERO;
        BigDecimal alternativeValue = BigDecimal.ZERO;
        
        for (SecurityHolding holding : holdings) {
            Security.AssetClass assetClass = holding.getSecurity().getAssetClass();
            BigDecimal marketValue = holding.getMarketValue() != null ? holding.getMarketValue() : BigDecimal.ZERO;
            
            switch (assetClass) {
                case EQUITY:
                    equityValue = equityValue.add(marketValue);
                    break;
                case FIXED_INCOME:
                    fixedIncomeValue = fixedIncomeValue.add(marketValue);
                    break;
                case ALTERNATIVE:
                case COMMODITY:
                case REAL_ESTATE:
                    alternativeValue = alternativeValue.add(marketValue);
                    break;
                case DERIVATIVE:
                case CASH_EQUIVALENT:
                case CURRENCY:
                    // Treat derivatives, cash equivalents, and currencies as alternatives
                    alternativeValue = alternativeValue.add(marketValue);
                    break;
            }
        }
        
        BigDecimal totalValue = portfolio.getTotalMarketValue();
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            portfolio.setEquityAllocation(equityValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            portfolio.setFixedIncomeAllocation(fixedIncomeValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            portfolio.setAlternativeAllocation(alternativeValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            portfolio.setCashAllocation(portfolio.getCashBalance().divide(totalValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        }
    }

    private BigDecimal calculateIssuerExposure(Portfolio portfolio, String issuer) {
        List<SecurityHolding> holdings = securityHoldingRepository.findByPortfolio(portfolio);
        
        BigDecimal issuerExposure = BigDecimal.ZERO;
        for (SecurityHolding holding : holdings) {
            if (issuer.equals(holding.getSecurity().getIssuer()) && holding.getWeightInPortfolio() != null) {
                issuerExposure = issuerExposure.add(holding.getWeightInPortfolio());
            }
        }
        
        return issuerExposure;
    }

    private void setDefaultLimits(Portfolio portfolio, Portfolio.RiskProfile riskProfile) {
        switch (riskProfile) {
            case CONSERVATIVE:
                portfolio.setMaxPositionSize(new BigDecimal("5.00"));
                portfolio.setMaxSectorExposure(new BigDecimal("15.00"));
                portfolio.setMaxSingleIssuer(new BigDecimal("3.00"));
                break;
            case MODERATE:
                portfolio.setMaxPositionSize(new BigDecimal("10.00"));
                portfolio.setMaxSectorExposure(new BigDecimal("25.00"));
                portfolio.setMaxSingleIssuer(new BigDecimal("5.00"));
                break;
            case AGGRESSIVE:
                portfolio.setMaxPositionSize(new BigDecimal("20.00"));
                portfolio.setMaxSectorExposure(new BigDecimal("40.00"));
                portfolio.setMaxSingleIssuer(new BigDecimal("10.00"));
                break;
            default:
                portfolio.setMaxPositionSize(new BigDecimal("10.00"));
                portfolio.setMaxSectorExposure(new BigDecimal("25.00"));
                portfolio.setMaxSingleIssuer(new BigDecimal("5.00"));
        }
    }

    public Map<String, Object> getPortfolioSummary(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("portfolioNumber", portfolio.getPortfolioNumber());
        summary.put("portfolioName", portfolio.getPortfolioName());
        summary.put("totalMarketValue", portfolio.getTotalMarketValue());
        summary.put("totalCost", portfolio.getTotalCost());
        summary.put("unrealizedPnl", portfolio.getUnrealizedPnl());
        summary.put("realizedPnl", portfolio.getRealizedPnl());
        summary.put("totalReturn", portfolio.getTotalReturn());
        summary.put("annualizedReturn", portfolio.getAnnualizedReturn());
        summary.put("cashBalance", portfolio.getCashBalance());
        summary.put("numberOfHoldings", securityHoldingRepository.countByPortfolio(portfolio));
        summary.put("lastValuationDate", portfolio.getLastValuationDate());
        
        return summary;
    }

    private String generatePortfolioNumber() {
        return "PF" + System.currentTimeMillis();
    }

    private String generateTransactionNumber(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    public List<Portfolio> getPortfoliosByCustomer(UUID customerId) {
        return portfolioRepository.findByCustomerId(customerId);
    }

    public List<SecurityTransaction> getPortfolioTransactions(UUID portfolioId) {
        return securityTransactionRepository.findByPortfolioIdOrderByTradeDateDesc(portfolioId);
    }

    public List<SecurityHolding> getPortfolioHoldings(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        return securityHoldingRepository.findByPortfolio(portfolio);
    }
}

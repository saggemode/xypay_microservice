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

@Service
@Transactional
public class TreasuryService {

    @Autowired
    private TreasuryOperationRepository treasuryOperationRepository;
    
    @Autowired
    private TreasuryPositionRepository treasuryPositionRepository;
    
    @Autowired
    private TreasurySettlementRepository treasurySettlementRepository;
    
    @Autowired
    private BankRepository bankRepository;
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    @Autowired
    private NotificationService notificationService;

    // Money Market Operations
    public TreasuryOperation createMoneyMarketDeposit(Long bankId, BigDecimal amount, String currencyCode,
                                                    BigDecimal interestRate, LocalDateTime maturityDate,
                                                    String counterparty) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TreasuryOperation operation = new TreasuryOperation();
        operation.setBank(bank);
        operation.setOperationNumber(generateOperationNumber("MM"));
        operation.setOperationType(TreasuryOperation.OperationType.MONEY_MARKET);
        operation.setInstrumentType(TreasuryOperation.InstrumentType.TERM_DEPOSIT);
        operation.setAmount(amount);
        operation.setCurrencyCode(currencyCode);
        operation.setInterestRate(interestRate);
        operation.setMaturityDate(maturityDate);
        operation.setValueDate(LocalDateTime.now().plusDays(2)); // T+2 settlement
        operation.setSettlementDate(operation.getValueDate());
        operation.setCounterparty(counterparty);
        operation.setCounterpartyRating(TreasuryOperation.CreditRating.BBB);
        operation.setStatus(TreasuryOperation.OperationStatus.PENDING);
        operation.setTradeDate(LocalDateTime.now());
        operation.setBookingDate(LocalDateTime.now());
        operation.setBaselClassification(TreasuryOperation.BaselClassification.BANKING_BOOK);
        operation.setIfrsClassification(TreasuryOperation.IfrsClassification.AMORTIZED_COST);
        
        // Calculate regulatory capital requirement
        calculateRegulatoryCapital(operation);
        
        operation = treasuryOperationRepository.save(operation);
        
        // Start approval workflow for large amounts
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            try {
                workflowEngineService.startWorkflow("TREASURY_APPROVAL", "TREASURY_OPERATION", 
                    operation.getId(), 1L, null);
            } catch (Exception e) {
                // Log error but don't fail creation
            }
        }
        
        return operation;
    }

    // Foreign Exchange Operations
    public TreasuryOperation createFXSpot(Long bankId, BigDecimal amount, String baseCurrency, 
                                        String quoteCurrency, BigDecimal spotRate, String counterparty) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TreasuryOperation operation = new TreasuryOperation();
        operation.setBank(bank);
        operation.setOperationNumber(generateOperationNumber("FX"));
        operation.setOperationType(TreasuryOperation.OperationType.FOREIGN_EXCHANGE);
        operation.setInstrumentType(TreasuryOperation.InstrumentType.FX_SPOT);
        operation.setAmount(amount);
        operation.setCurrencyCode(baseCurrency);
        operation.setBaseCurrency(baseCurrency);
        operation.setQuoteCurrency(quoteCurrency);
        operation.setSpotRate(spotRate);
        operation.setExchangeRate(spotRate);
        operation.setBaseCurrencyAmount(amount.multiply(spotRate));
        operation.setValueDate(LocalDateTime.now().plusDays(2)); // T+2 settlement
        operation.setSettlementDate(operation.getValueDate());
        operation.setCounterparty(counterparty);
        operation.setCounterpartyRating(TreasuryOperation.CreditRating.A);
        operation.setStatus(TreasuryOperation.OperationStatus.PENDING);
        operation.setTradeDate(LocalDateTime.now());
        operation.setBookingDate(LocalDateTime.now());
        operation.setBaselClassification(TreasuryOperation.BaselClassification.TRADING_BOOK);
        operation.setIfrsClassification(TreasuryOperation.IfrsClassification.FAIR_VALUE_PL);
        
        // Calculate FX risk metrics
        calculateFXRiskMetrics(operation);
        
        return treasuryOperationRepository.save(operation);
    }

    public TreasuryOperation createFXForward(Long bankId, BigDecimal amount, String baseCurrency,
                                           String quoteCurrency, BigDecimal forwardRate, 
                                           LocalDateTime maturityDate, String counterparty) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TreasuryOperation operation = new TreasuryOperation();
        operation.setBank(bank);
        operation.setOperationNumber(generateOperationNumber("FXF"));
        operation.setOperationType(TreasuryOperation.OperationType.FOREIGN_EXCHANGE);
        operation.setInstrumentType(TreasuryOperation.InstrumentType.FX_FORWARD);
        operation.setAmount(amount);
        operation.setCurrencyCode(baseCurrency);
        operation.setBaseCurrency(baseCurrency);
        operation.setQuoteCurrency(quoteCurrency);
        operation.setForwardRate(forwardRate);
        operation.setExchangeRate(forwardRate);
        operation.setMaturityDate(maturityDate);
        operation.setValueDate(maturityDate);
        operation.setSettlementDate(maturityDate);
        operation.setCounterparty(counterparty);
        operation.setCounterpartyRating(TreasuryOperation.CreditRating.A);
        operation.setStatus(TreasuryOperation.OperationStatus.PENDING);
        operation.setTradeDate(LocalDateTime.now());
        operation.setBookingDate(LocalDateTime.now());
        operation.setBaselClassification(TreasuryOperation.BaselClassification.TRADING_BOOK);
        operation.setIfrsClassification(TreasuryOperation.IfrsClassification.FAIR_VALUE_PL);
        
        // Calculate forward points
        BigDecimal spotRate = getCurrentSpotRate(baseCurrency, quoteCurrency);
        operation.setSpotRate(spotRate);
        operation.setForwardPoints(forwardRate.subtract(spotRate));
        
        // Calculate derivatives risk metrics
        calculateDerivativesRiskMetrics(operation);
        
        return treasuryOperationRepository.save(operation);
    }

    // Interest Rate Derivatives
    public TreasuryOperation createInterestRateSwap(Long bankId, BigDecimal notionalAmount, String currency,
                                                  BigDecimal fixedRate, String floatingRateIndex,
                                                  LocalDateTime maturityDate, String counterparty) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TreasuryOperation operation = new TreasuryOperation();
        operation.setBank(bank);
        operation.setOperationNumber(generateOperationNumber("IRS"));
        operation.setOperationType(TreasuryOperation.OperationType.DERIVATIVES);
        operation.setInstrumentType(TreasuryOperation.InstrumentType.INTEREST_RATE_SWAP);
        operation.setAmount(notionalAmount);
        operation.setCurrencyCode(currency);
        operation.setInterestRate(fixedRate);
        operation.setMaturityDate(maturityDate);
        operation.setValueDate(LocalDateTime.now().plusDays(2));
        operation.setSettlementDate(operation.getValueDate());
        operation.setCounterparty(counterparty);
        operation.setCounterpartyRating(TreasuryOperation.CreditRating.AA);
        operation.setStatus(TreasuryOperation.OperationStatus.PENDING);
        operation.setTradeDate(LocalDateTime.now());
        operation.setBookingDate(LocalDateTime.now());
        operation.setBaselClassification(TreasuryOperation.BaselClassification.TRADING_BOOK);
        operation.setIfrsClassification(TreasuryOperation.IfrsClassification.FAIR_VALUE_PL);
        
        // Calculate swap risk metrics
        calculateSwapRiskMetrics(operation);
        
        return treasuryOperationRepository.save(operation);
    }

    // Options
    public TreasuryOperation createFXOption(Long bankId, BigDecimal notionalAmount, String baseCurrency,
                                          String quoteCurrency, BigDecimal strikePrice, BigDecimal premium,
                                          TreasuryOperation.OptionType optionType, LocalDateTime expiryDate,
                                          String counterparty) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TreasuryOperation operation = new TreasuryOperation();
        operation.setBank(bank);
        operation.setOperationNumber(generateOperationNumber("OPT"));
        operation.setOperationType(TreasuryOperation.OperationType.DERIVATIVES);
        operation.setInstrumentType(TreasuryOperation.InstrumentType.OPTION);
        operation.setAmount(notionalAmount);
        operation.setCurrencyCode(baseCurrency);
        operation.setBaseCurrency(baseCurrency);
        operation.setQuoteCurrency(quoteCurrency);
        operation.setStrikePrice(strikePrice);
        operation.setPremium(premium);
        operation.setOptionType(optionType);
        operation.setExpiryDate(expiryDate);
        operation.setExerciseStyle(TreasuryOperation.ExerciseStyle.EUROPEAN);
        operation.setValueDate(LocalDateTime.now().plusDays(2));
        operation.setCounterparty(counterparty);
        operation.setCounterpartyRating(TreasuryOperation.CreditRating.A);
        operation.setStatus(TreasuryOperation.OperationStatus.PENDING);
        operation.setTradeDate(LocalDateTime.now());
        operation.setBookingDate(LocalDateTime.now());
        operation.setBaselClassification(TreasuryOperation.BaselClassification.TRADING_BOOK);
        operation.setIfrsClassification(TreasuryOperation.IfrsClassification.FAIR_VALUE_PL);
        
        // Calculate option Greeks
        calculateOptionGreeks(operation);
        
        return treasuryOperationRepository.save(operation);
    }

    // Islamic Treasury Operations
    public TreasuryOperation createSukukInvestment(Long bankId, BigDecimal amount, String currencyCode,
                                                 BigDecimal expectedReturn, LocalDateTime maturityDate,
                                                 TreasuryOperation.IslamicStructure structure) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TreasuryOperation operation = new TreasuryOperation();
        operation.setBank(bank);
        operation.setOperationNumber(generateOperationNumber("SUK"));
        operation.setOperationType(TreasuryOperation.OperationType.MONEY_MARKET);
        operation.setInstrumentType(TreasuryOperation.InstrumentType.GOVERNMENT_BOND);
        operation.setAmount(amount);
        operation.setCurrencyCode(currencyCode);
        operation.setInterestRate(expectedReturn); // Expected return rate
        operation.setMaturityDate(maturityDate);
        operation.setValueDate(LocalDateTime.now().plusDays(2));
        operation.setSettlementDate(operation.getValueDate());
        operation.setStatus(TreasuryOperation.OperationStatus.PENDING);
        operation.setTradeDate(LocalDateTime.now());
        operation.setBookingDate(LocalDateTime.now());
        operation.setShariaCompliant(true);
        operation.setIslamicStructure(structure);
        operation.setProfitSharingRatio(new BigDecimal("70")); // 70% to investor, 30% to issuer
        operation.setBaselClassification(TreasuryOperation.BaselClassification.AFS);
        operation.setIfrsClassification(TreasuryOperation.IfrsClassification.FAIR_VALUE_OCI);
        
        return treasuryOperationRepository.save(operation);
    }

    public TreasuryOperation approveOperation(Long operationId, Long approvedBy) {
        TreasuryOperation operation = treasuryOperationRepository.findById(operationId)
            .orElseThrow(() -> new RuntimeException("Treasury operation not found"));
        
        operation.setStatus(TreasuryOperation.OperationStatus.APPROVED);
        operation.setApprovedBy(approvedBy);
        operation.setApprovalDate(LocalDateTime.now());
        
        return treasuryOperationRepository.save(operation);
    }

    public TreasuryOperation executeOperation(Long operationId) {
        TreasuryOperation operation = treasuryOperationRepository.findById(operationId)
            .orElseThrow(() -> new RuntimeException("Treasury operation not found"));
        
        if (operation.getStatus() != TreasuryOperation.OperationStatus.APPROVED) {
            throw new RuntimeException("Operation must be approved before execution");
        }
        
        operation.setStatus(TreasuryOperation.OperationStatus.EXECUTED);
        
        // Create position
        createPosition(operation);
        
        // Create settlement instruction
        createSettlement(operation);
        
        // Update market value
        updateMarketValuation(operation);
        
        return treasuryOperationRepository.save(operation);
    }

    private void createPosition(TreasuryOperation operation) {
        TreasuryPosition position = new TreasuryPosition();
        position.setTreasuryOperation(operation);
        position.setPositionDate(LocalDateTime.now());
        position.setQuantity(operation.getAmount());
        position.setUnitPrice(BigDecimal.ONE);
        position.setMarketPrice(BigDecimal.ONE);
        position.setPositionValue(operation.getAmount());
        position.setPositionType(TreasuryPosition.PositionType.LONG);
        
        treasuryPositionRepository.save(position);
    }

    private void createSettlement(TreasuryOperation operation) {
        TreasurySettlement settlement = new TreasurySettlement();
        settlement.setTreasuryOperation(operation);
        settlement.setSettlementDate(operation.getSettlementDate());
        settlement.setSettlementAmount(operation.getAmount());
        settlement.setSettlementCurrency(operation.getCurrencyCode());
        settlement.setSettlementStatus(TreasurySettlement.SettlementStatus.PENDING);
        settlement.setSettlementReference(generateSettlementReference());
        settlement.setSettlementMethod(TreasurySettlement.SettlementMethod.WIRE_TRANSFER);
        
        treasurySettlementRepository.save(settlement);
    }

    private void updateMarketValuation(TreasuryOperation operation) {
        // Simulate market valuation
        BigDecimal marketValue = operation.getAmount();
        
        // Add some market movement simulation
        BigDecimal marketMovement = new BigDecimal("0.01"); // 1% movement
        if (operation.getOperationType() == TreasuryOperation.OperationType.FOREIGN_EXCHANGE) {
            marketValue = marketValue.multiply(BigDecimal.ONE.add(marketMovement));
        }
        
        operation.setMarketValue(marketValue);
        operation.setBookValue(operation.getAmount());
        operation.setUnrealizedPnl(marketValue.subtract(operation.getAmount()));
        operation.setLastValuationDate(LocalDateTime.now());
    }

    private void calculateRegulatoryCapital(TreasuryOperation operation) {
        // Simplified Basel III capital calculation
        BigDecimal riskWeight = new BigDecimal("0.20"); // 20% risk weight for bank deposits
        operation.setRiskWeight(riskWeight);
        operation.setRegulatoryCapital(operation.getAmount().multiply(riskWeight).multiply(new BigDecimal("0.08"))); // 8% capital ratio
    }

    private void calculateFXRiskMetrics(TreasuryOperation operation) {
        // Simplified VaR calculation (1% of notional)
        operation.setVarAmount(operation.getAmount().multiply(new BigDecimal("0.01")));
        
        // FX delta (sensitivity to spot rate changes)
        operation.setDelta(BigDecimal.ONE);
    }

    private void calculateDerivativesRiskMetrics(TreasuryOperation operation) {
        // Forward delta
        operation.setDelta(BigDecimal.ONE);
        
        // Duration for interest rate sensitivity
        long daysToMaturity = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDateTime.now(), operation.getMaturityDate());
        operation.setDuration(new BigDecimal(daysToMaturity).divide(new BigDecimal("365"), 4, RoundingMode.HALF_UP));
        
        // VaR calculation
        operation.setVarAmount(operation.getAmount().multiply(new BigDecimal("0.02")));
    }

    private void calculateSwapRiskMetrics(TreasuryOperation operation) {
        // Interest rate duration
        long yearsToMaturity = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDateTime.now(), operation.getMaturityDate()) / 365;
        operation.setDuration(new BigDecimal(yearsToMaturity));
        
        // Convexity (simplified)
        operation.setConvexity(operation.getDuration().multiply(operation.getDuration()));
        
        // VaR for interest rate swaps
        operation.setVarAmount(operation.getAmount().multiply(new BigDecimal("0.015")));
    }

    private void calculateOptionGreeks(TreasuryOperation operation) {
        // Simplified Black-Scholes Greeks (would need proper implementation)
        operation.setDelta(new BigDecimal("0.5")); // At-the-money delta
        operation.setGamma(new BigDecimal("0.1"));
        operation.setTheta(new BigDecimal("-0.05")); // Time decay
        operation.setVega(new BigDecimal("0.2")); // Volatility sensitivity
        operation.setRho(new BigDecimal("0.1")); // Interest rate sensitivity
        
        // Option VaR
        operation.setVarAmount(operation.getPremium().multiply(new BigDecimal("0.3")));
    }

    private BigDecimal getCurrentSpotRate(String baseCurrency, String quoteCurrency) {
        // Simulate spot rate lookup - in real implementation, this would call market data service
        if ("USD".equals(baseCurrency) && "EUR".equals(quoteCurrency)) {
            return new BigDecimal("0.85");
        } else if ("EUR".equals(baseCurrency) && "USD".equals(quoteCurrency)) {
            return new BigDecimal("1.18");
        }
        return BigDecimal.ONE;
    }

    public void performDailyValuation() {
        List<TreasuryOperation> activeOperations = treasuryOperationRepository
            .findByStatusIn(List.of(TreasuryOperation.OperationStatus.EXECUTED));
        
        for (TreasuryOperation operation : activeOperations) {
            updateMarketValuation(operation);
            treasuryOperationRepository.save(operation);
        }
    }

    public Map<String, Object> getTreasuryPortfolioMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        List<TreasuryOperation> activeOperations = treasuryOperationRepository
            .findByStatusIn(List.of(TreasuryOperation.OperationStatus.EXECUTED));
        
        BigDecimal totalNotional = activeOperations.stream()
            .map(TreasuryOperation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalMarketValue = activeOperations.stream()
            .map(op -> op.getMarketValue() != null ? op.getMarketValue() : op.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalUnrealizedPnl = activeOperations.stream()
            .map(op -> op.getUnrealizedPnl() != null ? op.getUnrealizedPnl() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalVaR = activeOperations.stream()
            .map(op -> op.getVarAmount() != null ? op.getVarAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        metrics.put("totalNotional", totalNotional);
        metrics.put("totalMarketValue", totalMarketValue);
        metrics.put("totalUnrealizedPnl", totalUnrealizedPnl);
        metrics.put("totalVaR", totalVaR);
        metrics.put("numberOfPositions", activeOperations.size());
        metrics.put("averagePositionSize", activeOperations.isEmpty() ? BigDecimal.ZERO : 
            totalNotional.divide(new BigDecimal(activeOperations.size()), 2, RoundingMode.HALF_UP));
        
        return metrics;
    }

    private String generateOperationNumber(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    private String generateSettlementReference() {
        return "SETT" + System.currentTimeMillis();
    }

    public List<TreasuryOperation> getOperationsByType(TreasuryOperation.OperationType operationType) {
        return treasuryOperationRepository.findByOperationType(operationType);
    }

    public List<TreasuryOperation> getMaturingOperations(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().plusDays(days);
        return treasuryOperationRepository.findByMaturityDateBeforeAndStatusIn(
            cutoffDate, 
            List.of(TreasuryOperation.OperationStatus.EXECUTED)
        );
    }
}

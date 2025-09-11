package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoanManagementService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private LoanProductRepository loanProductRepository;
    
    @Autowired
    private LoanAmortizationRepository loanAmortizationRepository;
    
    @Autowired
    private LoanRepaymentRepository loanRepaymentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    @Autowired
    private NotificationService notificationService;

    public Loan createLoanApplication(Long customerId, String productCode, BigDecimal amount, 
                                    Integer termMonths, String currencyCode) {
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
            
        LoanProduct product = loanProductRepository.findByProductCode(productCode)
            .orElseThrow(() -> new RuntimeException("Loan product not found"));
            
        // Validate loan amount
        if (amount.compareTo(product.getMinimumAmount()) < 0 || 
            amount.compareTo(product.getMaximumAmount()) > 0) {
            throw new RuntimeException("Loan amount outside product limits");
        }
        
        // Validate term
        if (termMonths < product.getMinimumTermMonths() || 
            termMonths > product.getMaximumTermMonths()) {
            throw new RuntimeException("Loan term outside product limits");
        }
        
        Loan loan = new Loan();
        loan.setLoanProduct(product);
        loan.setCustomer(customer);
        loan.setLoanNumber(generateLoanNumber());
        loan.setPrincipalAmount(amount);
        loan.setOutstandingPrincipal(amount);
        loan.setTotalOutstanding(amount);
        loan.setCurrencyCode(currencyCode);
        loan.setInterestRate(product.getInterestRate());
        loan.setLoanTermMonths(termMonths);
        loan.setRemainingTermMonths(termMonths);
        loan.setApplicationDate(LocalDateTime.now());
        loan.setStatus(Loan.LoanStatus.APPLIED);
        loan.setRepaymentFrequency(Loan.RepaymentFrequency.valueOf(product.getRepaymentFrequency().name()));
        loan.setRiskRating(Loan.RiskRating.STANDARD);
        loan.setImpairmentStage(Loan.ImpairmentStage.STAGE_1);
        loan.setBaselClassification(Loan.BaselClassification.STANDARD);
        loan.setIfrsClassification(Loan.IfrsClassification.STAGE_1);
        loan.setShariaCompliant(product.getShariaCompliant());
        
        if (product.getShariaCompliant()) {
            loan.setIslamicStructure(Loan.IslamicStructure.valueOf(product.getIslamicStructure().name()));
            loan.setProfitRate(product.getInterestRate()); // Profit rate for Islamic loans
        }
        
        loan = loanRepository.save(loan);
        
        // Start approval workflow
        try {
            workflowEngineService.startWorkflow("LOAN_APPROVAL", "LOAN", loan.getId(), 
                customerId, null);
        } catch (Exception e) {
            // Log error but don't fail loan creation
        }
        
        return loan;
    }

    public Loan approveLoan(Long loanId, Long approvedBy) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
            
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDateTime.now());
        loan.setApprovedBy(approvedBy);
        
        // Calculate maturity date
        LocalDateTime maturityDate = loan.getApplicationDate().plusMonths(loan.getLoanTermMonths());
        loan.setMaturityDate(maturityDate);
        
        // Calculate monthly payment
        BigDecimal monthlyPayment = calculateMonthlyPayment(loan);
        loan.setMonthlyPaymentAmount(monthlyPayment);
        
        loan = loanRepository.save(loan);
        
        // Generate amortization schedule
        generateAmortizationSchedule(loan);
        
        return loan;
    }

    public Loan disburseLoan(Long loanId, BigDecimal amount) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
            
        if (loan.getStatus() != Loan.LoanStatus.APPROVED) {
            throw new RuntimeException("Loan must be approved before disbursement");
        }
        
        loan.setDisbursedAmount(amount);
        loan.setDisbursementDate(LocalDateTime.now());
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        
        // Set first payment date
        LocalDateTime firstPaymentDate = calculateNextPaymentDate(loan.getDisbursementDate(), 
            loan.getRepaymentFrequency());
        loan.setFirstPaymentDate(firstPaymentDate);
        loan.setNextPaymentDate(firstPaymentDate);
        
        return loanRepository.save(loan);
    }

    public List<LoanAmortization> generateAmortizationSchedule(Loan loan) {
        List<LoanAmortization> schedule = new ArrayList<>();
        
        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal monthlyRate = loan.getInterestRate().divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP)
            .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = loan.getMonthlyPaymentAmount();
        
        LocalDateTime paymentDate = loan.getFirstPaymentDate();
        BigDecimal remainingBalance = principal;
        
        for (int i = 1; i <= loan.getLoanTermMonths(); i++) {
            LoanAmortization amortization = new LoanAmortization();
            amortization.setLoan(loan);
            amortization.setInstallmentNumber(i);
            amortization.setDueDate(paymentDate);
            amortization.setOpeningBalance(remainingBalance);
            
            BigDecimal interestAmount;
            BigDecimal principalAmount;
            
            if (loan.getShariaCompliant()) {
                // Islamic banking calculation - profit rate
                interestAmount = remainingBalance.multiply(loan.getProfitRate())
                    .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                amortization.setProfitAmount(interestAmount);
            } else {
                // Conventional interest calculation
                interestAmount = remainingBalance.multiply(monthlyRate);
            }
            
            principalAmount = monthlyPayment.subtract(interestAmount);
            
            // Adjust for last payment
            if (i == loan.getLoanTermMonths()) {
                principalAmount = remainingBalance;
                monthlyPayment = principalAmount.add(interestAmount);
            }
            
            amortization.setInterestAmount(interestAmount);
            amortization.setPrincipalAmount(principalAmount);
            amortization.setTotalPayment(monthlyPayment);
            
            remainingBalance = remainingBalance.subtract(principalAmount);
            amortization.setClosingBalance(remainingBalance);
            
            schedule.add(amortization);
            
            // Calculate next payment date
            paymentDate = calculateNextPaymentDate(paymentDate, loan.getRepaymentFrequency());
        }
        
        // Save schedule
        loanAmortizationRepository.saveAll(schedule);
        
        return schedule;
    }

    public BigDecimal calculateMonthlyPayment(Loan loan) {
        if (loan.getShariaCompliant()) {
            return calculateIslamicMonthlyPayment(loan);
        }
        
        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal monthlyRate = loan.getInterestRate().divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP)
            .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        int numPayments = loan.getLoanTermMonths();
        
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(numPayments), 2, RoundingMode.HALF_UP);
        }
        
        // PMT formula: P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRatePowN = onePlusRate.pow(numPayments);
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowN);
        BigDecimal denominator = onePlusRatePowN.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateIslamicMonthlyPayment(Loan loan) {
        // Simplified Islamic calculation - equal installments
        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal totalProfit = principal.multiply(loan.getProfitRate())
            .multiply(new BigDecimal(loan.getLoanTermMonths()))
            .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
        BigDecimal totalAmount = principal.add(totalProfit);
        return totalAmount.divide(new BigDecimal(loan.getLoanTermMonths()), 2, RoundingMode.HALF_UP);
    }

    public void processLoanRepayment(Long loanId, BigDecimal amount, String paymentMethod, 
                                   String transactionReference) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
            
        // Get next due payment
        Optional<LoanAmortization> nextDue = loanAmortizationRepository
            .findFirstByLoanAndIsPaidFalseOrderByDueDateAsc(loan);
            
        if (nextDue.isEmpty()) {
            throw new RuntimeException("No pending payments found");
        }
        
        LoanAmortization amortization = nextDue.get();
        
        // Create repayment record
        LoanRepayment repayment = new LoanRepayment();
        repayment.setLoan(loan);
        repayment.setPaymentNumber(amortization.getInstallmentNumber());
        repayment.setPaymentDate(LocalDateTime.now());
        repayment.setDueDate(amortization.getDueDate());
        repayment.setPrincipalAmount(amortization.getPrincipalAmount());
        repayment.setInterestAmount(amortization.getInterestAmount());
        repayment.setTotalAmount(amortization.getTotalPayment());
        repayment.setPaidAmount(amount);
        repayment.setPaymentMethod(paymentMethod);
        repayment.setTransactionReference(transactionReference);
        
        // Calculate days overdue
        long daysOverdue = ChronoUnit.DAYS.between(amortization.getDueDate(), LocalDateTime.now());
        if (daysOverdue > 0) {
            repayment.setDaysOverdue((int) daysOverdue);
            // Calculate late fee
            BigDecimal lateFee = calculateLateFee(loan, amortization.getTotalPayment(), (int) daysOverdue);
            repayment.setLateFee(lateFee);
        }
        
        // Determine payment status
        if (amount.compareTo(amortization.getTotalPayment()) >= 0) {
            repayment.setPaymentStatus(LoanRepayment.PaymentStatus.PAID);
            repayment.setOutstandingAmount(BigDecimal.ZERO);
            amortization.setIsPaid(true);
            amortization.setPaymentDate(LocalDateTime.now());
        } else {
            repayment.setPaymentStatus(LoanRepayment.PaymentStatus.PARTIAL);
            repayment.setOutstandingAmount(amortization.getTotalPayment().subtract(amount));
        }
        
        loanRepaymentRepository.save(repayment);
        loanAmortizationRepository.save(amortization);
        
        // Update loan balances
        updateLoanBalances(loan);
        
        // Update next payment date if fully paid
        if (repayment.getPaymentStatus() == LoanRepayment.PaymentStatus.PAID) {
            Optional<LoanAmortization> nextPayment = loanAmortizationRepository
                .findFirstByLoanAndIsPaidFalseOrderByDueDateAsc(loan);
            if (nextPayment.isPresent()) {
                loan.setNextPaymentDate(nextPayment.get().getDueDate());
            } else {
                // Loan fully paid
                loan.setStatus(Loan.LoanStatus.CLOSED);
                loan.setNextPaymentDate(null);
            }
            loan.setLastPaymentDate(LocalDateTime.now());
            loanRepository.save(loan);
        }
    }

    private void updateLoanBalances(Loan loan) {
        // Calculate outstanding principal and interest
        List<LoanAmortization> unpaidInstallments = loanAmortizationRepository
            .findByLoanAndIsPaidFalseOrderByDueDateAsc(loan);
            
        BigDecimal outstandingPrincipal = unpaidInstallments.stream()
            .map(LoanAmortization::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal outstandingInterest = unpaidInstallments.stream()
            .map(LoanAmortization::getInterestAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        loan.setOutstandingPrincipal(outstandingPrincipal);
        loan.setOutstandingInterest(outstandingInterest);
        loan.setTotalOutstanding(outstandingPrincipal.add(outstandingInterest));
        loan.setRemainingTermMonths(unpaidInstallments.size());
        
        // Update risk rating based on days past due
        updateRiskRating(loan);
        
        loanRepository.save(loan);
    }

    private void updateRiskRating(Loan loan) {
        // Calculate maximum days past due
        List<LoanRepayment> overduePayments = loanRepaymentRepository
            .findByLoanAndPaymentStatusAndDaysOverdueGreaterThan(loan, 
                LoanRepayment.PaymentStatus.OVERDUE, 0);
                
        int maxDaysPastDue = overduePayments.stream()
            .mapToInt(LoanRepayment::getDaysOverdue)
            .max().orElse(0);
            
        loan.setDaysPastDue(maxDaysPastDue);
        
        // Update risk rating and classifications
        if (maxDaysPastDue == 0) {
            loan.setRiskRating(Loan.RiskRating.STANDARD);
            loan.setBaselClassification(Loan.BaselClassification.STANDARD);
            loan.setImpairmentStage(Loan.ImpairmentStage.STAGE_1);
        } else if (maxDaysPastDue <= 30) {
            loan.setRiskRating(Loan.RiskRating.STANDARD);
            loan.setBaselClassification(Loan.BaselClassification.SPECIAL_MENTION);
            loan.setImpairmentStage(Loan.ImpairmentStage.STAGE_2);
        } else if (maxDaysPastDue <= 90) {
            loan.setRiskRating(Loan.RiskRating.SUBSTANDARD);
            loan.setBaselClassification(Loan.BaselClassification.SUBSTANDARD);
            loan.setImpairmentStage(Loan.ImpairmentStage.STAGE_3);
        } else if (maxDaysPastDue <= 180) {
            loan.setRiskRating(Loan.RiskRating.DOUBTFUL);
            loan.setBaselClassification(Loan.BaselClassification.DOUBTFUL);
            loan.setImpairmentStage(Loan.ImpairmentStage.STAGE_3);
        } else {
            loan.setRiskRating(Loan.RiskRating.LOSS);
            loan.setBaselClassification(Loan.BaselClassification.LOSS);
            loan.setImpairmentStage(Loan.ImpairmentStage.STAGE_3);
        }
        
        // Calculate provisioning
        calculateProvisioning(loan);
    }

    private void calculateProvisioning(Loan loan) {
        BigDecimal provisionRate = BigDecimal.ZERO;
        
        // Basel III provisioning rates
        switch (loan.getBaselClassification()) {
            case STANDARD:
                provisionRate = new BigDecimal("0.01"); // 1%
                break;
            case SPECIAL_MENTION:
                provisionRate = new BigDecimal("0.05"); // 5%
                break;
            case SUBSTANDARD:
                provisionRate = new BigDecimal("0.20"); // 20%
                break;
            case DOUBTFUL:
                provisionRate = new BigDecimal("0.50"); // 50%
                break;
            case LOSS:
                provisionRate = new BigDecimal("1.00"); // 100%
                break;
        }
        
        BigDecimal provisionAmount = loan.getOutstandingPrincipal().multiply(provisionRate);
        loan.setProvisionRate(provisionRate);
        loan.setProvisionAmount(provisionAmount);
    }

    private BigDecimal calculateLateFee(Loan loan, BigDecimal paymentAmount, int daysOverdue) {
        LoanProduct product = loan.getLoanProduct();
        if (product.getLatePaymentPenaltyRate().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate daily penalty rate
        BigDecimal dailyPenaltyRate = product.getLatePaymentPenaltyRate()
            .divide(new BigDecimal("365"), 6, RoundingMode.HALF_UP);
            
        return paymentAmount.multiply(dailyPenaltyRate)
            .multiply(new BigDecimal(daysOverdue))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime calculateNextPaymentDate(LocalDateTime currentDate, Loan.RepaymentFrequency frequency) {
        switch (frequency) {
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case BI_WEEKLY:
                return currentDate.plusWeeks(2);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case QUARTERLY:
                return currentDate.plusMonths(3);
            case SEMI_ANNUALLY:
                return currentDate.plusMonths(6);
            case ANNUALLY:
                return currentDate.plusYears(1);
            default:
                return currentDate.plusMonths(1);
        }
    }

    private String generateLoanNumber() {
        return "LN" + System.currentTimeMillis();
    }

    public List<Loan> getOverdueLoans() {
        return loanRepository.findByStatusAndDaysPastDueGreaterThan(
            Loan.LoanStatus.ACTIVE, 0);
    }

    public List<Loan> getLoansByRiskRating(Loan.RiskRating riskRating) {
        return loanRepository.findByRiskRating(riskRating);
    }

    public BigDecimal getTotalProvisionAmount() {
        return loanRepository.findAll().stream()
            .map(Loan::getProvisionAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package com.xypay.xypay.service;

import com.xypay.xypay.domain.Loan;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.LoanProduct;
import com.xypay.xypay.repository.LoanRepository;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.LoanProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoanProductRepository loanProductRepository;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    @Autowired(required = false)
    private KafkaEventService kafkaEventService;

    public Loan originateLoan(Long customerId, Long productId, BigDecimal amount) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        LoanProduct product = loanProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Loan product not found"));
        
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanProduct(product);
        loan.setPrincipalAmount(amount);
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loan.setApplicationDate(LocalDateTime.now());
        loan.setCurrencyCode(product.getCurrencyCode());
        loan.setInterestRate(product.getInterestRate());
        
        Loan saved = loanRepository.save(loan);
        auditTrailService.logEvent("LOAN_ORIGINATED", "Loan originated: " + saved.getId() + ", amount: " + amount);
        if (kafkaEventService != null) {
            kafkaEventService.publishEvent("loans", String.valueOf(saved.getId()), "Loan originated: " + saved.getId());
        }
        return saved;
    }
    public Loan getLoan(Long loanId) {
        return loanRepository.findById(loanId).orElse(null);
    }
    public void disburseLoan(Long loanId) {
        Loan loan = getLoan(loanId);
        if (loan != null) {
            loan.setStatus(Loan.LoanStatus.DISBURSED);
            loan.setDisbursementDate(LocalDateTime.now());
            loanRepository.save(loan);
            auditTrailService.logEvent("LOAN_DISBURSED", "Loan disbursed: " + loanId);
            if (kafkaEventService != null) {
                kafkaEventService.publishEvent("loans", String.valueOf(loanId), "Loan disbursed: " + loanId);
            }
        }
    }
}
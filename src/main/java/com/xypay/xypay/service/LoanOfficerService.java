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
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LoanOfficerService {
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoanProductRepository loanProductRepository;

    public String applyForLoan(UUID customerId, UUID productId, BigDecimal amount) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        LoanProduct product = loanProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Loan product not found"));
        
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanProduct(product);
        loan.setPrincipalAmount(amount);
        loan.setStatus(Loan.LoanStatus.APPLIED);
        loan.setApplicationDate(LocalDateTime.now());
        loan.setCurrencyCode(product.getCurrencyCode());
        loan.setInterestRate(product.getInterestRate());
        
        loanRepository.save(loan);
        return "Loan application submitted for customer ID: " + customerId;
    }

    public String creditCheck(UUID customerId) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return "Credit check completed for customer: " + customer.getUsername();
    }

    public List<Loan> getPortfolio() {
        return loanRepository.findAll();
    }

    public String riskAssessment(UUID customerId) {
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return "Risk assessment completed for customer: " + customer.getUsername();
    }

    public void approveLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loanRepository.save(loan);
    }

    public void rejectLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(Loan.LoanStatus.REJECTED);
        loanRepository.save(loan);
    }

    public String getRepaymentSchedule(UUID loanId) {
        return "Repayment schedule for loan " + loanId + " will be displayed here.";
    }

    public int getCreditScore(String customerName) {
        // Mock credit score: random between 600 and 800
        return 600 + (int)(Math.random() * 200);
    }
}

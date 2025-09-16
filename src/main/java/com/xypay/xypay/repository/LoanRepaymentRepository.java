package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Loan;
import com.xypay.xypay.domain.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, UUID> {
    
    List<LoanRepayment> findByLoanOrderByPaymentDateDesc(Loan loan);
    
    List<LoanRepayment> findByLoanAndPaymentStatus(Loan loan, LoanRepayment.PaymentStatus status);
    
    List<LoanRepayment> findByLoanAndPaymentStatusAndDaysOverdueGreaterThan(Loan loan, 
                                                                           LoanRepayment.PaymentStatus status, 
                                                                           Integer daysOverdue);
    
    List<LoanRepayment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<LoanRepayment> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<LoanRepayment> findByTransactionReference(String transactionReference);
    
    @Query("SELECT SUM(lr.paidAmount) FROM LoanRepayment lr WHERE lr.loan = :loan")
    BigDecimal getTotalPaidAmount(@Param("loan") Loan loan);
    
    @Query("SELECT lr FROM LoanRepayment lr WHERE lr.loan.id = :loanId ORDER BY lr.paymentDate DESC")
    List<LoanRepayment> findByLoanIdOrderByPaymentDateDesc(@Param("loanId") Long loanId);
    
    @Query("SELECT COUNT(lr) FROM LoanRepayment lr WHERE lr.paymentStatus = :status")
    Long countByPaymentStatus(@Param("status") LoanRepayment.PaymentStatus status);
}

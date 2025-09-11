package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Loan;
import com.xypay.xypay.domain.LoanAmortization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAmortizationRepository extends JpaRepository<LoanAmortization, Long> {
    
    List<LoanAmortization> findByLoanOrderByInstallmentNumber(Loan loan);
    
    List<LoanAmortization> findByLoanAndIsPaidFalseOrderByDueDateAsc(Loan loan);
    
    Optional<LoanAmortization> findFirstByLoanAndIsPaidFalseOrderByDueDateAsc(Loan loan);
    
    List<LoanAmortization> findByLoanAndIsPaidTrueOrderByInstallmentNumber(Loan loan);
    
    List<LoanAmortization> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<LoanAmortization> findByDueDateBeforeAndIsPaidFalse(LocalDateTime date);
    
    @Query("SELECT la FROM LoanAmortization la WHERE la.loan = :loan AND la.installmentNumber = :number")
    Optional<LoanAmortization> findByLoanAndInstallmentNumber(@Param("loan") Loan loan, 
                                                            @Param("number") Integer installmentNumber);
    
    @Query("SELECT COUNT(la) FROM LoanAmortization la WHERE la.loan = :loan AND la.isPaid = false")
    Long countUnpaidInstallments(@Param("loan") Loan loan);
    
    @Query("SELECT la FROM LoanAmortization la WHERE la.loan.id = :loanId ORDER BY la.installmentNumber")
    List<LoanAmortization> findByLoanIdOrderByInstallmentNumber(@Param("loanId") Long loanId);
}

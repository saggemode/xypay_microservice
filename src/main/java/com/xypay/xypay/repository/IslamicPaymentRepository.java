package com.xypay.xypay.repository;

import com.xypay.xypay.domain.IslamicPayment;
import com.xypay.xypay.domain.IslamicBankingContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IslamicPaymentRepository extends JpaRepository<IslamicPayment, UUID> {
    
    List<IslamicPayment> findByIslamicContract(IslamicBankingContract islamicContract);
    
    List<IslamicPayment> findByIslamicContractIdOrderByDueDateAsc(Long contractId);
    
    List<IslamicPayment> findByPaymentStatus(IslamicPayment.PaymentStatus paymentStatus);
    
    List<IslamicPayment> findByPaymentType(IslamicPayment.PaymentType paymentType);
    
    List<IslamicPayment> findByPaymentMethod(IslamicPayment.PaymentMethod paymentMethod);
    
    List<IslamicPayment> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<IslamicPayment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<IslamicPayment> findByDueDateBeforeAndPaymentStatus(LocalDateTime date, IslamicPayment.PaymentStatus status);
    
    List<IslamicPayment> findByDaysOverdueGreaterThan(Integer days);
    
    @Query("SELECT ip FROM IslamicPayment ip WHERE ip.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<IslamicPayment> findByTotalAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                              @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT COUNT(ip) FROM IslamicPayment ip WHERE ip.paymentStatus = :status")
    Long countByPaymentStatus(@Param("status") IslamicPayment.PaymentStatus status);
    
    @Query("SELECT SUM(ip.amountPaid) FROM IslamicPayment ip WHERE ip.paymentStatus = 'PAID'")
    BigDecimal getTotalPaidAmount();
    
    @Query("SELECT SUM(ip.outstandingAmount) FROM IslamicPayment ip WHERE ip.paymentStatus IN ('PENDING', 'PARTIAL', 'OVERDUE')")
    BigDecimal getTotalOutstandingAmount();
    
    @Query("SELECT SUM(ip.charityAmount) FROM IslamicPayment ip")
    BigDecimal getTotalCharityAmount();
}

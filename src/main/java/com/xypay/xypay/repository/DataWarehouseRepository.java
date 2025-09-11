package com.xypay.xypay.repository;

import com.xypay.xypay.domain.DataWarehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DataWarehouseRepository extends JpaRepository<DataWarehouse, Long> {
    
    Optional<DataWarehouse> findByFactDate(LocalDate factDate);
    
    List<DataWarehouse> findByFactDateBetweenOrderByFactDate(LocalDate startDate, LocalDate endDate);
    
    List<DataWarehouse> findByYearAndMonthOrderByFactDate(Integer year, Integer month);
    
    List<DataWarehouse> findByYearAndQuarterOrderByFactDate(Integer year, Integer quarter);
    
    @Query("SELECT SUM(dw.totalAmount) FROM DataWarehouse dw WHERE dw.factDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(dw.totalTransactions) FROM DataWarehouse dw WHERE dw.factDate BETWEEN :startDate AND :endDate")
    Double getAverageTransactionsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT dw FROM DataWarehouse dw WHERE dw.year = :year ORDER BY dw.totalAmount DESC")
    List<DataWarehouse> findTopTransactionDaysByYear(@Param("year") Integer year);
    
    @Query("SELECT SUM(dw.newCustomers) FROM DataWarehouse dw WHERE dw.factDate BETWEEN :startDate AND :endDate")
    Long getTotalNewCustomersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

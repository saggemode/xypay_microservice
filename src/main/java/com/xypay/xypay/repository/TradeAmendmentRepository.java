package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TradeAmendment;
import com.xypay.xypay.domain.TradeFinance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeAmendmentRepository extends JpaRepository<TradeAmendment, Long> {
    
    List<TradeAmendment> findByTradeFinance(TradeFinance tradeFinance);
    
    List<TradeAmendment> findByTradeFinanceOrderByAmendmentNumberDesc(TradeFinance tradeFinance);
    
    List<TradeAmendment> findByStatus(TradeAmendment.AmendmentStatus status);
    
    List<TradeAmendment> findByAmendmentType(TradeAmendment.AmendmentType amendmentType);
    
    Long countByTradeFinance(TradeFinance tradeFinance);
}

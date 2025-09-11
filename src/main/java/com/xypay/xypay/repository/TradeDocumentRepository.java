package com.xypay.xypay.repository;

import com.xypay.xypay.domain.TradeDocument;
import com.xypay.xypay.domain.TradeFinance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeDocumentRepository extends JpaRepository<TradeDocument, Long> {
    
    List<TradeDocument> findByTradeFinance(TradeFinance tradeFinance);
    
    List<TradeDocument> findByTradeFinanceAndStatus(TradeFinance tradeFinance, TradeDocument.DocumentStatus status);
    
    List<TradeDocument> findByDocumentType(TradeDocument.DocumentType documentType);
    
    List<TradeDocument> findByIsRequiredTrueAndIsReceivedFalse();
    
    Long countByTradeFinanceAndStatus(TradeFinance tradeFinance, TradeDocument.DocumentStatus status);
}

package com.xypay.treasury.service;

import com.xypay.treasury.domain.TreasuryPosition;
import com.xypay.treasury.dto.TreasuryPositionRequest;
import com.xypay.treasury.dto.TreasuryPositionResponse;
import com.xypay.treasury.repository.TreasuryPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreasuryService {
    
    private final TreasuryPositionRepository treasuryPositionRepository;
    
    @Transactional
    public TreasuryPositionResponse createPosition(TreasuryPositionRequest request) {
        log.info("Creating treasury position for currency: {}, amount: {}", 
                request.getCurrencyCode(), request.getPositionAmount());
        
        TreasuryPosition position = new TreasuryPosition();
        position.setCurrencyCode(request.getCurrencyCode());
        position.setPositionAmount(request.getPositionAmount());
        position.setAvailableAmount(request.getAvailableAmount() != null ? 
            request.getAvailableAmount() : request.getPositionAmount());
        position.setReservedAmount(request.getReservedAmount() != null ? 
            request.getReservedAmount() : BigDecimal.ZERO);
        position.setValueDate(request.getValueDate());
        position.setMaturityDate(request.getMaturityDate());
        position.setPositionType(TreasuryPosition.PositionType.valueOf(request.getPositionType()));
        position.setLiquidityBucket(TreasuryPosition.LiquidityBucket.valueOf(request.getLiquidityBucket()));
        position.setInterestRate(request.getInterestRate());
        position.setCostCenter(request.getCostCenter());
        position.setProfitCenter(request.getProfitCenter());
        position.setRiskWeight(request.getRiskWeight() != null ? 
            request.getRiskWeight() : new BigDecimal("100.00"));
        position.setIsActive(request.getIsActive());
        
        TreasuryPosition savedPosition = treasuryPositionRepository.save(position);
        log.info("Treasury position created with ID: {}", savedPosition.getId());
        
        return TreasuryPositionResponse.fromTreasuryPosition(savedPosition);
    }
    
    public TreasuryPositionResponse getPositionById(Long id) {
        log.info("Retrieving treasury position with ID: {}", id);
        
        TreasuryPosition position = treasuryPositionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Treasury position not found with ID: " + id));
        
        return TreasuryPositionResponse.fromTreasuryPosition(position);
    }
    
    public List<TreasuryPositionResponse> getAllPositions() {
        log.info("Retrieving all treasury positions");
        
        List<TreasuryPosition> positions = treasuryPositionRepository.findAll();
        
        return positions.stream()
            .map(TreasuryPositionResponse::fromTreasuryPosition)
            .collect(Collectors.toList());
    }
    
    public List<TreasuryPositionResponse> getPositionsByCurrency(String currencyCode) {
        log.info("Retrieving treasury positions for currency: {}", currencyCode);
        
        List<TreasuryPosition> positions = treasuryPositionRepository.findByCurrencyCode(currencyCode);
        
        return positions.stream()
            .map(TreasuryPositionResponse::fromTreasuryPosition)
            .collect(Collectors.toList());
    }
    
    public List<TreasuryPositionResponse> getActivePositions() {
        log.info("Retrieving active treasury positions");
        
        List<TreasuryPosition> positions = treasuryPositionRepository.findByIsActiveTrue();
        
        return positions.stream()
            .map(TreasuryPositionResponse::fromTreasuryPosition)
            .collect(Collectors.toList());
    }
    
    public List<TreasuryPositionResponse> getPositionsByType(String positionType) {
        log.info("Retrieving treasury positions by type: {}", positionType);
        
        List<TreasuryPosition> positions = treasuryPositionRepository
            .findByPositionType(TreasuryPosition.PositionType.valueOf(positionType));
        
        return positions.stream()
            .map(TreasuryPositionResponse::fromTreasuryPosition)
            .collect(Collectors.toList());
    }
    
    public List<TreasuryPositionResponse> getPositionsByLiquidityBucket(String liquidityBucket) {
        log.info("Retrieving treasury positions by liquidity bucket: {}", liquidityBucket);
        
        List<TreasuryPosition> positions = treasuryPositionRepository
            .findByLiquidityBucket(TreasuryPosition.LiquidityBucket.valueOf(liquidityBucket));
        
        return positions.stream()
            .map(TreasuryPositionResponse::fromTreasuryPosition)
            .collect(Collectors.toList());
    }
    
    public BigDecimal getTotalPositionAmountByCurrency(String currencyCode) {
        log.info("Calculating total position amount for currency: {}", currencyCode);
        
        return treasuryPositionRepository.getTotalPositionAmountByCurrency(currencyCode);
    }
    
    public BigDecimal getTotalAvailableAmountByCurrency(String currencyCode) {
        log.info("Calculating total available amount for currency: {}", currencyCode);
        
        return treasuryPositionRepository.getTotalAvailableAmountByCurrency(currencyCode);
    }
    
    public BigDecimal getTotalReservedAmountByCurrency(String currencyCode) {
        log.info("Calculating total reserved amount for currency: {}", currencyCode);
        
        return treasuryPositionRepository.getTotalReservedAmountByCurrency(currencyCode);
    }
    
    @Transactional
    public TreasuryPositionResponse updatePosition(Long id, TreasuryPositionRequest request) {
        log.info("Updating treasury position with ID: {}", id);
        
        TreasuryPosition position = treasuryPositionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Treasury position not found with ID: " + id));
        
        position.setCurrencyCode(request.getCurrencyCode());
        position.setPositionAmount(request.getPositionAmount());
        position.setAvailableAmount(request.getAvailableAmount());
        position.setReservedAmount(request.getReservedAmount());
        position.setValueDate(request.getValueDate());
        position.setMaturityDate(request.getMaturityDate());
        position.setPositionType(TreasuryPosition.PositionType.valueOf(request.getPositionType()));
        position.setLiquidityBucket(TreasuryPosition.LiquidityBucket.valueOf(request.getLiquidityBucket()));
        position.setInterestRate(request.getInterestRate());
        position.setCostCenter(request.getCostCenter());
        position.setProfitCenter(request.getProfitCenter());
        position.setRiskWeight(request.getRiskWeight());
        position.setIsActive(request.getIsActive());
        
        TreasuryPosition updatedPosition = treasuryPositionRepository.save(position);
        log.info("Treasury position updated with ID: {}", updatedPosition.getId());
        
        return TreasuryPositionResponse.fromTreasuryPosition(updatedPosition);
    }
    
    @Transactional
    public void deletePosition(Long id) {
        log.info("Deleting treasury position with ID: {}", id);
        
        if (!treasuryPositionRepository.existsById(id)) {
            throw new RuntimeException("Treasury position not found with ID: " + id);
        }
        
        treasuryPositionRepository.deleteById(id);
        log.info("Treasury position deleted with ID: {}", id);
    }
    
    @Transactional
    public void deactivatePosition(Long id) {
        log.info("Deactivating treasury position with ID: {}", id);
        
        TreasuryPosition position = treasuryPositionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Treasury position not found with ID: " + id));
        
        position.setIsActive(false);
        treasuryPositionRepository.save(position);
        log.info("Treasury position deactivated with ID: {}", id);
    }
}

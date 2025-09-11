package com.xypay.xypay.service;

import com.xypay.xypay.domain.Branch;
import com.xypay.xypay.domain.Bank;
import com.xypay.xypay.repository.BranchRepository;
import com.xypay.xypay.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private BankRepository bankRepository;
    public Branch createBranch(Long bankId, String name, String code, String address) {
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        Branch branch = new Branch();
        branch.setBank(bank);
        branch.setName(name);
        branch.setCode(code);
        branch.setAddress(address);
        branch.setIsActive(true);
        branch.setBranchType(Branch.BranchType.FULL_SERVICE);
        branch.setEstablishedDate(LocalDateTime.now());
        branch.setSupports24x7(false);
        branch.setAutomatedProcessing(true);
        branch.setRealTimeProcessing(true);
        
        return branchRepository.save(branch);
    }
    public Optional<Branch> getBranch(Long id) {
        return branchRepository.findById(id);
    }
    
    public Branch getBranchById(Long id) {
        return branchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Branch not found"));
    }
    public List<Branch> listBranches() {
        return branchRepository.findAll();
    }
    
    public List<Branch> listActiveBranches() {
        return branchRepository.findByIsActiveTrue();
    }
    
    public List<Branch> listBranchesByBank(Long bankId) {
        return branchRepository.findByBankId(bankId);
    }
    public void setActive(Long id, boolean active) {
        Branch branch = getBranchById(id);
        branch.setIsActive(active);
        branchRepository.save(branch);
    }
    
    public Branch updateBranch(Long id, String name, String address, String phone, String email) {
        Branch branch = getBranchById(id);
        
        if (name != null) branch.setName(name);
        if (address != null) branch.setAddress(address);
        if (phone != null) branch.setPhone(phone);
        if (email != null) branch.setEmail(email);
        
        return branchRepository.save(branch);
    }
    
    public void setBranchLimits(Long id, BigDecimal cashLimit, BigDecimal dailyTransactionLimit) {
        Branch branch = getBranchById(id);
        branch.setCashLimit(cashLimit);
        branch.setDailyTransactionLimit(dailyTransactionLimit);
        branchRepository.save(branch);
    }
    
    public void setBranchLocation(Long id, BigDecimal latitude, BigDecimal longitude) {
        Branch branch = getBranchById(id);
        branch.setLatitude(latitude);
        branch.setLongitude(longitude);
        branchRepository.save(branch);
    }
}
package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
@Transactional
public class IslamicBankingService {

    @Autowired
    private IslamicBankingProductRepository islamicProductRepository;
    
    @Autowired
    private IslamicBankingContractRepository islamicContractRepository;
    
    @Autowired
    private IslamicPaymentRepository islamicPaymentRepository;
    
    @Autowired
    private IslamicProfitDistributionRepository profitDistributionRepository;
    
    @Autowired
    private BankRepository bankRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Removed unused services to fix warnings

    // Product Management
    public IslamicBankingProduct createIslamicProduct(UUID bankId, String productCode, String productName,
                                                     IslamicBankingProduct.ProductCategory category,
                                                     IslamicBankingProduct.IslamicStructure structure,
                                                     BigDecimal minAmount, BigDecimal maxAmount,
                                                     String currencyCode, BigDecimal profitSharingRatio) {
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        IslamicBankingProduct product = new IslamicBankingProduct();
        product.setBank(bank);
        product.setProductCode(productCode);
        product.setProductName(productName);
        product.setProductCategory(category);
        product.setIslamicStructure(structure);
        product.setMinimumAmount(minAmount);
        product.setMaximumAmount(maxAmount);
        product.setCurrencyCode(currencyCode);
        product.setProfitSharingRatio(profitSharingRatio);
        product.setBankSharingRatio(new BigDecimal("100").subtract(profitSharingRatio));
        product.setLaunchDate(LocalDateTime.now());
        product.setIsActive(true);
        
        // Set default values based on structure
        setDefaultProductParameters(product, structure);
        
        return islamicProductRepository.save(product);
    }

    // Murabaha (Cost-Plus Sale) Contract
    public IslamicBankingContract createMurabahaContract(UUID productId, UUID customerId, 
                                                       BigDecimal principalAmount, Integer termMonths,
                                                       BigDecimal profitRate, String underlyingAsset) {
        
        IslamicBankingProduct product = islamicProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Islamic product not found"));
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (product.getIslamicStructure() != IslamicBankingProduct.IslamicStructure.MURABAHA) {
            throw new RuntimeException("Product is not a Murabaha structure");
        }
        
        IslamicBankingContract contract = new IslamicBankingContract();
        contract.setIslamicProduct(product);
        contract.setCustomer(customer);
        contract.setBank(product.getBank());
        contract.setContractNumber(generateContractNumber("MUR"));
        contract.setContractStatus(IslamicBankingContract.ContractStatus.DRAFT);
        contract.setContractDate(LocalDateTime.now());
        contract.setPrincipalAmount(principalAmount);
        contract.setCurrencyCode(product.getCurrencyCode());
        contract.setProfitRate(profitRate);
        contract.setUnderlyingAsset(underlyingAsset);
        contract.setAssetValue(principalAmount);
        
        // Calculate total profit and payable amount
        BigDecimal totalProfit = calculateMurabahaProfit(principalAmount, profitRate, termMonths);
        contract.setTotalProfitExpected(totalProfit);
        contract.setTotalAmountPayable(principalAmount.add(totalProfit));
        contract.setOutstandingPrincipal(principalAmount);
        contract.setOutstandingProfit(totalProfit);
        
        // Calculate monthly installment
        BigDecimal monthlyInstallment = contract.getTotalAmountPayable()
            .divide(new BigDecimal(termMonths), 2, RoundingMode.HALF_UP);
        contract.setMonthlyInstallment(monthlyInstallment);
        
        // Set maturity date
        contract.setMaturityDate(LocalDateTime.now().plusMonths(termMonths));
        
        return islamicContractRepository.save(contract);
    }

    // Ijara (Lease) Contract
    public IslamicBankingContract createIjaraContract(UUID productId, UUID customerId,
                                                    BigDecimal assetValue, Integer termMonths,
                                                    BigDecimal rentalRate, String assetDescription) {
        
        IslamicBankingProduct product = islamicProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Islamic product not found"));
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (product.getIslamicStructure() != IslamicBankingProduct.IslamicStructure.IJARA) {
            throw new RuntimeException("Product is not an Ijara structure");
        }
        
        IslamicBankingContract contract = new IslamicBankingContract();
        contract.setIslamicProduct(product);
        contract.setCustomer(customer);
        contract.setBank(product.getBank());
        contract.setContractNumber(generateContractNumber("IJA"));
        contract.setContractStatus(IslamicBankingContract.ContractStatus.DRAFT);
        contract.setContractDate(LocalDateTime.now());
        contract.setPrincipalAmount(assetValue);
        contract.setCurrencyCode(product.getCurrencyCode());
        contract.setUnderlyingAsset(assetDescription);
        contract.setAssetValue(assetValue);
        contract.setRentalFrequency(IslamicBankingContract.RentalFrequency.MONTHLY);
        contract.setMaintenanceResponsibility(IslamicBankingContract.MaintenanceResponsibility.CUSTOMER);
        contract.setInsuranceResponsibility(IslamicBankingContract.InsuranceResponsibility.CUSTOMER);
        
        // Calculate monthly rental
        BigDecimal monthlyRental = assetValue.multiply(rentalRate)
            .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        contract.setRentalAmount(monthlyRental);
        contract.setMonthlyInstallment(monthlyRental);
        
        // Set maturity date
        contract.setMaturityDate(LocalDateTime.now().plusMonths(termMonths));
        
        // Total rental over the term
        BigDecimal totalRental = monthlyRental.multiply(new BigDecimal(termMonths));
        contract.setTotalAmountPayable(totalRental);
        contract.setOutstandingPrincipal(totalRental);
        
        return islamicContractRepository.save(contract);
    }

    // Musharaka (Partnership) Contract
    public IslamicBankingContract createMushараkaContract(UUID productId, UUID customerId,
                                                        BigDecimal bankContribution, BigDecimal customerContribution,
                                                        BigDecimal bankProfitShare, String businessActivity) {
        
        IslamicBankingProduct product = islamicProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Islamic product not found"));
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (product.getIslamicStructure() != IslamicBankingProduct.IslamicStructure.MUSHARAKA) {
            throw new RuntimeException("Product is not a Musharaka structure");
        }
        
        IslamicBankingContract contract = new IslamicBankingContract();
        contract.setIslamicProduct(product);
        contract.setCustomer(customer);
        contract.setBank(product.getBank());
        contract.setContractNumber(generateContractNumber("MUS"));
        contract.setContractStatus(IslamicBankingContract.ContractStatus.DRAFT);
        contract.setContractDate(LocalDateTime.now());
        contract.setBankCapitalContribution(bankContribution);
        contract.setCustomerCapitalContribution(customerContribution);
        contract.setPrincipalAmount(bankContribution.add(customerContribution));
        contract.setCurrencyCode(product.getCurrencyCode());
        contract.setBankProfitShare(bankProfitShare);
        contract.setCustomerProfitShare(new BigDecimal("100").subtract(bankProfitShare));
        contract.setBusinessActivity(businessActivity);
        contract.setManagementResponsibility(IslamicBankingContract.ManagementResponsibility.SHARED);
        
        // No fixed maturity for partnership
        contract.setMaturityDate(LocalDateTime.now().plusYears(5)); // Default 5 years
        
        return islamicContractRepository.save(contract);
    }

    // Mudaraba (Profit-Sharing) Contract
    public IslamicBankingContract createMudараbaContract(UUID productId, UUID customerId,
                                                       BigDecimal capitalAmount, BigDecimal bankProfitShare,
                                                       String businessActivity) {
        
        IslamicBankingProduct product = islamicProductRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Islamic product not found"));
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (product.getIslamicStructure() != IslamicBankingProduct.IslamicStructure.MUDARABA) {
            throw new RuntimeException("Product is not a Mudaraba structure");
        }
        
        IslamicBankingContract contract = new IslamicBankingContract();
        contract.setIslamicProduct(product);
        contract.setCustomer(customer);
        contract.setBank(product.getBank());
        contract.setContractNumber(generateContractNumber("MUD"));
        contract.setContractStatus(IslamicBankingContract.ContractStatus.DRAFT);
        contract.setContractDate(LocalDateTime.now());
        contract.setBankCapitalContribution(capitalAmount);
        contract.setCustomerCapitalContribution(BigDecimal.ZERO);
        contract.setPrincipalAmount(capitalAmount);
        contract.setCurrencyCode(product.getCurrencyCode());
        contract.setBankProfitShare(bankProfitShare);
        contract.setCustomerProfitShare(new BigDecimal("100").subtract(bankProfitShare));
        contract.setBusinessActivity(businessActivity);
        contract.setManagementResponsibility(IslamicBankingContract.ManagementResponsibility.CUSTOMER);
        
        // Set maturity date
        contract.setMaturityDate(LocalDateTime.now().plusYears(3)); // Default 3 years
        
        return islamicContractRepository.save(contract);
    }

    public IslamicBankingContract approveContract(UUID contractId, UUID approvedBy) {
        IslamicBankingContract contract = islamicContractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Islamic contract not found"));
        
        contract.setContractStatus(IslamicBankingContract.ContractStatus.APPROVED);
        contract.setApprovedBy(approvedBy.getMostSignificantBits()); // Convert UUID to Long
        contract.setApprovalDate(LocalDateTime.now());
        contract.setEffectiveDate(LocalDateTime.now());
        
        // Generate payment schedule for fixed payment structures
        if (isFixedPaymentStructure(contract.getIslamicProduct().getIslamicStructure())) {
            generatePaymentSchedule(contract);
        }
        
        return islamicContractRepository.save(contract);
    }

    public IslamicBankingContract activateContract(UUID contractId) {
        IslamicBankingContract contract = islamicContractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Islamic contract not found"));
        
        if (contract.getContractStatus() != IslamicBankingContract.ContractStatus.APPROVED) {
            throw new RuntimeException("Contract must be approved before activation");
        }
        
        contract.setContractStatus(IslamicBankingContract.ContractStatus.ACTIVE);
        contract.setDisbursementDate(LocalDateTime.now());
        
        // Set next payment date
        if (contract.getMonthlyInstallment() != null) {
            contract.setNextPaymentDate(LocalDateTime.now().plusMonths(1));
        }
        
        return islamicContractRepository.save(contract);
    }

    // Payment Processing
    public IslamicPayment processPayment(UUID contractId, BigDecimal paymentAmount, 
                                       IslamicPayment.PaymentMethod paymentMethod, String paymentReference) {
        
        IslamicBankingContract contract = islamicContractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Islamic contract not found"));
        
        IslamicPayment payment = new IslamicPayment();
        payment.setIslamicContract(contract);
        payment.setPaymentType(IslamicPayment.PaymentType.INSTALLMENT);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTotalAmount(paymentAmount);
        payment.setAmountPaid(paymentAmount);
        payment.setPaymentStatus(IslamicPayment.PaymentStatus.PAID);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentReference(paymentReference);
        
        // Allocate payment between principal and profit
        allocatePayment(contract, payment, paymentAmount);
        
        payment = islamicPaymentRepository.save(payment);
        
        // Update contract balances
        updateContractBalances(contract, paymentAmount);
        
        // Update next payment date
        if (contract.getNextPaymentDate() != null) {
            contract.setNextPaymentDate(contract.getNextPaymentDate().plusMonths(1));
        }
        
        islamicContractRepository.save(contract);
        
        return payment;
    }

    // Profit Distribution for Partnership Contracts
    public IslamicProfitDistribution distributeProfits(UUID contractId, BigDecimal grossProfit,
                                                     BigDecimal allowableExpenses, LocalDateTime periodStart,
                                                     LocalDateTime periodEnd) {
        
        IslamicBankingContract contract = islamicContractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Islamic contract not found"));
        
        if (!isProfitSharingStructure(contract.getIslamicProduct().getIslamicStructure())) {
            throw new RuntimeException("Contract is not a profit-sharing structure");
        }
        
        BigDecimal netProfit = grossProfit.subtract(allowableExpenses);
        
        IslamicProfitDistribution distribution = new IslamicProfitDistribution();
        distribution.setIslamicContract(contract);
        distribution.setDistributionDate(LocalDateTime.now());
        distribution.setPeriodStartDate(periodStart);
        distribution.setPeriodEndDate(periodEnd);
        distribution.setGrossProfit(grossProfit);
        distribution.setAllowableExpenses(allowableExpenses);
        distribution.setNetProfit(netProfit);
        distribution.setCustomerSharePercentage(contract.getCustomerProfitShare());
        distribution.setBankSharePercentage(contract.getBankProfitShare());
        
        // Calculate profit amounts
        BigDecimal customerProfitAmount = netProfit.multiply(contract.getCustomerProfitShare())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal bankProfitAmount = netProfit.subtract(customerProfitAmount);
        
        distribution.setCustomerProfitAmount(customerProfitAmount);
        distribution.setBankProfitAmount(bankProfitAmount);
        distribution.setDistributionStatus(IslamicProfitDistribution.DistributionStatus.CALCULATED);
        
        // Update contract actual profit
        contract.setActualProfitEarned(contract.getActualProfitEarned().add(netProfit));
        islamicContractRepository.save(contract);
        
        return profitDistributionRepository.save(distribution);
    }

    // Sharia Compliance Verification
    public boolean verifyShariaCompliance(UUID contractId, String shariaAdvisor) {
        IslamicBankingContract contract = islamicContractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Islamic contract not found"));
        
        // Perform Sharia compliance checks
        boolean isCompliant = performShariaComplianceChecks(contract);
        
        if (isCompliant) {
            contract.setShariaBoardApproved(true);
            contract.setShariaApprovalDate(LocalDateTime.now());
            contract.setShariaAdvisor(shariaAdvisor);
            contract.setShariaApprovalReference(generateShariaReference());
            contract.setAaoifiCompliance(true);
            
            islamicContractRepository.save(contract);
        }
        
        return isCompliant;
    }

    // Risk Management
    public void updateRiskRating(UUID contractId, IslamicBankingContract.RiskRating newRating) {
        IslamicBankingContract contract = islamicContractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Islamic contract not found"));
        
        contract.setRiskRating(newRating);
        
        // Update IFRS stage based on risk rating
        if (newRating == IslamicBankingContract.RiskRating.SUBSTANDARD ||
            newRating == IslamicBankingContract.RiskRating.DOUBTFUL) {
            contract.setIfrsStage(IslamicBankingContract.IfrsStage.STAGE_2);
        } else if (newRating == IslamicBankingContract.RiskRating.LOSS) {
            contract.setIfrsStage(IslamicBankingContract.IfrsStage.STAGE_3);
        }
        
        // Calculate provision
        calculateProvision(contract);
        
        islamicContractRepository.save(contract);
    }

    private void setDefaultProductParameters(IslamicBankingProduct product, 
                                           IslamicBankingProduct.IslamicStructure structure) {
        switch (structure) {
            case MURABAHA:
                product.setProfitCalculationMethod(IslamicBankingProduct.ProfitCalculationMethod.COST_PLUS_MARKUP);
                product.setPaymentFrequency(IslamicBankingProduct.PaymentFrequency.MONTHLY);
                product.setMinimumTermMonths(12);
                product.setMaximumTermMonths(84);
                break;
            case IJARA:
                product.setProfitCalculationMethod(IslamicBankingProduct.ProfitCalculationMethod.RENTAL_BASED);
                product.setPaymentFrequency(IslamicBankingProduct.PaymentFrequency.MONTHLY);
                product.setMinimumTermMonths(12);
                product.setMaximumTermMonths(120);
                break;
            case MUSHARAKA:
            case MUDARABA:
                product.setProfitCalculationMethod(IslamicBankingProduct.ProfitCalculationMethod.PROFIT_SHARING);
                product.setPaymentFrequency(IslamicBankingProduct.PaymentFrequency.QUARTERLY);
                product.setMinimumTermMonths(36);
                product.setMaximumTermMonths(120);
                break;
            default:
                product.setProfitCalculationMethod(IslamicBankingProduct.ProfitCalculationMethod.FIXED_RATE);
                product.setPaymentFrequency(IslamicBankingProduct.PaymentFrequency.MONTHLY);
        }
    }

    private BigDecimal calculateMurabahaProfit(BigDecimal principalAmount, BigDecimal profitRate, Integer termMonths) {
        // Simple profit calculation for Murabaha
        BigDecimal annualProfit = principalAmount.multiply(profitRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyProfit = annualProfit.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        return monthlyProfit.multiply(new BigDecimal(termMonths));
    }

    private void generatePaymentSchedule(IslamicBankingContract contract) {
        if (contract.getMonthlyInstallment() == null || contract.getMaturityDate() == null) {
            return;
        }
        
        LocalDateTime paymentDate = contract.getEffectiveDate().plusMonths(1);
        int paymentNumber = 1;
        
        while (paymentDate.isBefore(contract.getMaturityDate()) || paymentDate.isEqual(contract.getMaturityDate())) {
            IslamicPayment scheduledPayment = new IslamicPayment();
            scheduledPayment.setIslamicContract(contract);
            scheduledPayment.setPaymentNumber(paymentNumber);
            scheduledPayment.setPaymentType(IslamicPayment.PaymentType.INSTALLMENT);
            scheduledPayment.setDueDate(paymentDate);
            scheduledPayment.setTotalAmount(contract.getMonthlyInstallment());
            scheduledPayment.setOutstandingAmount(contract.getMonthlyInstallment());
            scheduledPayment.setPaymentStatus(IslamicPayment.PaymentStatus.PENDING);
            
            // Allocate between principal and profit
            allocateScheduledPayment(contract, scheduledPayment);
            
            islamicPaymentRepository.save(scheduledPayment);
            
            paymentDate = paymentDate.plusMonths(1);
            paymentNumber++;
        }
    }

    private void allocatePayment(IslamicBankingContract contract, IslamicPayment payment, BigDecimal paymentAmount) {
        IslamicBankingProduct.IslamicStructure structure = contract.getIslamicProduct().getIslamicStructure();
        
        if (structure == IslamicBankingProduct.IslamicStructure.IJARA) {
            payment.setRentalAmount(paymentAmount);
        } else if (structure == IslamicBankingProduct.IslamicStructure.MURABAHA) {
            // Allocate between principal and profit
            BigDecimal profitPortion = paymentAmount.multiply(new BigDecimal("0.3")); // Simplified
            BigDecimal principalPortion = paymentAmount.subtract(profitPortion);
            
            payment.setProfitAmount(profitPortion);
            payment.setPrincipalAmount(principalPortion);
        }
    }

    private void allocateScheduledPayment(IslamicBankingContract contract, IslamicPayment payment) {
        IslamicBankingProduct.IslamicStructure structure = contract.getIslamicProduct().getIslamicStructure();
        
        if (structure == IslamicBankingProduct.IslamicStructure.IJARA) {
            payment.setRentalAmount(payment.getTotalAmount());
        } else if (structure == IslamicBankingProduct.IslamicStructure.MURABAHA) {
            // Calculate principal and profit portions
            BigDecimal totalPayments = contract.getTotalAmountPayable();
            BigDecimal profitRatio = contract.getTotalProfitExpected().divide(totalPayments, 4, RoundingMode.HALF_UP);
            
            BigDecimal profitPortion = payment.getTotalAmount().multiply(profitRatio);
            BigDecimal principalPortion = payment.getTotalAmount().subtract(profitPortion);
            
            payment.setProfitAmount(profitPortion);
            payment.setPrincipalAmount(principalPortion);
        }
    }

    private void updateContractBalances(IslamicBankingContract contract, BigDecimal paymentAmount) {
        contract.setTotalPaid(contract.getTotalPaid().add(paymentAmount));
        
        // Update outstanding amounts based on payment allocation
        // This is simplified - in practice, would need more complex allocation logic
        BigDecimal remainingAmount = contract.getTotalAmountPayable().subtract(contract.getTotalPaid());
        contract.setOutstandingPrincipal(remainingAmount);
    }

    private boolean isFixedPaymentStructure(IslamicBankingProduct.IslamicStructure structure) {
        return structure == IslamicBankingProduct.IslamicStructure.MURABAHA ||
               structure == IslamicBankingProduct.IslamicStructure.IJARA;
    }

    private boolean isProfitSharingStructure(IslamicBankingProduct.IslamicStructure structure) {
        return structure == IslamicBankingProduct.IslamicStructure.MUSHARAKA ||
               structure == IslamicBankingProduct.IslamicStructure.MUDARABA;
    }

    private boolean performShariaComplianceChecks(IslamicBankingContract contract) {
        // Simplified Sharia compliance checks
        IslamicBankingProduct.IslamicStructure structure = contract.getIslamicProduct().getIslamicStructure();
        
        switch (structure) {
            case MURABAHA:
                // Check if underlying asset is Sharia-compliant
                return contract.getUnderlyingAsset() != null && !contract.getUnderlyingAsset().isEmpty();
            case IJARA:
                // Check if asset ownership is properly transferred
                return contract.getAssetValue() != null && contract.getAssetValue().compareTo(BigDecimal.ZERO) > 0;
            case MUSHARAKA:
            case MUDARABA:
                // Check if business activity is Sharia-compliant
                return contract.getBusinessActivity() != null && !contract.getBusinessActivity().isEmpty();
            default:
                return true;
        }
    }

    private void calculateProvision(IslamicBankingContract contract) {
        BigDecimal provisionRate = BigDecimal.ZERO;
        
        switch (contract.getRiskRating()) {
            case STANDARD:
                provisionRate = new BigDecimal("1.00");
                break;
            case SUBSTANDARD:
                provisionRate = new BigDecimal("20.00");
                break;
            case DOUBTFUL:
                provisionRate = new BigDecimal("50.00");
                break;
            case LOSS:
                provisionRate = new BigDecimal("100.00");
                break;
            default:
                provisionRate = new BigDecimal("0.50");
        }
        
        BigDecimal provisionAmount = contract.getOutstandingPrincipal()
            .multiply(provisionRate)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        contract.setProvisionAmount(provisionAmount);
    }

    public Map<String, Object> getIslamicBankingMetrics(UUID bankId) {
        Map<String, Object> metrics = new HashMap<>();
        
        List<IslamicBankingContract> activeContracts = islamicContractRepository
            .findByBankIdAndContractStatus(bankId, IslamicBankingContract.ContractStatus.ACTIVE);
        
        BigDecimal totalFinancing = activeContracts.stream()
            .map(IslamicBankingContract::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalOutstanding = activeContracts.stream()
            .map(IslamicBankingContract::getOutstandingPrincipal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalProvisions = activeContracts.stream()
            .map(contract -> contract.getProvisionAmount() != null ? contract.getProvisionAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        metrics.put("totalFinancing", totalFinancing);
        metrics.put("totalOutstanding", totalOutstanding);
        metrics.put("totalProvisions", totalProvisions);
        metrics.put("numberOfContracts", activeContracts.size());
        metrics.put("averageContractSize", activeContracts.isEmpty() ? BigDecimal.ZERO : 
            totalFinancing.divide(new BigDecimal(activeContracts.size()), 2, RoundingMode.HALF_UP));
        
        return metrics;
    }

    private String generateContractNumber(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    private String generateShariaReference() {
        return "SHA" + System.currentTimeMillis();
    }

    public List<IslamicBankingContract> getContractsByCustomer(UUID customerId) {
        return islamicContractRepository.findByCustomerId(customerId);
    }

    public List<IslamicPayment> getContractPayments(UUID contractId) {
        return islamicPaymentRepository.findByIslamicContractIdOrderByDueDateAsc(contractId);
    }

    public List<IslamicBankingProduct> getActiveIslamicProducts(UUID bankId) {
        return islamicProductRepository.findByBankIdAndIsActiveTrue(bankId);
    }
}

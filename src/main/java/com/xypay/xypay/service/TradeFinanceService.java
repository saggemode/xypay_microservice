package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class TradeFinanceService {

    @Autowired
    private TradeFinanceRepository tradeFinanceRepository;
    
    @Autowired
    private TradeDocumentRepository tradeDocumentRepository;
    
    @Autowired
    private TradeAmendmentRepository tradeAmendmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BankRepository bankRepository;
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    @Autowired
    private NotificationService notificationService;

    public TradeFinance createLetterOfCredit(Long customerId, Long bankId, BigDecimal amount, 
                                           String currencyCode, String beneficiaryName, 
                                           String beneficiaryBank, LocalDateTime expiryDate) {
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TradeFinance lc = new TradeFinance();
        lc.setBank(bank);
        lc.setCustomer(customer);
        lc.setReferenceNumber(generateReferenceNumber("LC"));
        lc.setTradeType(TradeFinance.TradeType.IMPORT);
        lc.setInstrumentType(TradeFinance.InstrumentType.LETTER_OF_CREDIT);
        lc.setAmount(amount);
        lc.setCurrencyCode(currencyCode);
        lc.setStatus(TradeFinance.TradeStatus.DRAFT);
        lc.setLcNumber(generateLCNumber());
        lc.setApplicantName(customer.getFirstName() + " " + customer.getLastName());
        lc.setBeneficiaryName(beneficiaryName);
        lc.setBeneficiaryBank(beneficiaryBank);
        lc.setIssueDate(LocalDateTime.now());
        lc.setExpiryDate(expiryDate);
        lc.setPresentationPeriodDays(21); // Standard 21 days
        lc.setCountryRiskRating(TradeFinance.RiskRating.MEDIUM);
        lc.setCustomerRiskRating(TradeFinance.RiskRating.LOW);
        lc.setSanctionsScreeningStatus(TradeFinance.ScreeningStatus.PENDING);
        lc.setAmlScreeningStatus(TradeFinance.ScreeningStatus.PENDING);
        
        // Calculate margins and charges
        calculateLCCharges(lc);
        
        lc = tradeFinanceRepository.save(lc);
        
        // Start approval workflow
        try {
            workflowEngineService.startWorkflow("TRADE_FINANCE_APPROVAL", "TRADE_FINANCE", 
                lc.getId(), customerId, null);
        } catch (Exception e) {
            // Log error but don't fail creation
        }
        
        return lc;
    }

    public TradeFinance createGuarantee(Long customerId, Long bankId, BigDecimal amount, 
                                      String currencyCode, TradeFinance.GuaranteeType guaranteeType,
                                      String beneficiaryName, LocalDateTime expiryDate) {
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TradeFinance guarantee = new TradeFinance();
        guarantee.setBank(bank);
        guarantee.setCustomer(customer);
        guarantee.setReferenceNumber(generateReferenceNumber("BG"));
        guarantee.setTradeType(TradeFinance.TradeType.DOMESTIC);
        guarantee.setInstrumentType(TradeFinance.InstrumentType.GUARANTEE);
        guarantee.setAmount(amount);
        guarantee.setCurrencyCode(currencyCode);
        guarantee.setStatus(TradeFinance.TradeStatus.DRAFT);
        guarantee.setGuaranteeType(guaranteeType);
        guarantee.setPrincipalName(customer.getFirstName() + " " + customer.getLastName());
        guarantee.setBeneficiaryName(beneficiaryName);
        guarantee.setIssueDate(LocalDateTime.now());
        guarantee.setExpiryDate(expiryDate);
        guarantee.setClaimExpiryDate(expiryDate.plusDays(30)); // 30 days after expiry for claims
        guarantee.setCountryRiskRating(TradeFinance.RiskRating.LOW);
        guarantee.setCustomerRiskRating(TradeFinance.RiskRating.LOW);
        guarantee.setSanctionsScreeningStatus(TradeFinance.ScreeningStatus.PENDING);
        guarantee.setAmlScreeningStatus(TradeFinance.ScreeningStatus.PENDING);
        
        // Calculate margins and charges
        calculateGuaranteeCharges(guarantee);
        
        guarantee = tradeFinanceRepository.save(guarantee);
        
        // Start approval workflow
        try {
            workflowEngineService.startWorkflow("TRADE_FINANCE_APPROVAL", "TRADE_FINANCE", 
                guarantee.getId(), customerId, null);
        } catch (Exception e) {
            // Log error but don't fail creation
        }
        
        return guarantee;
    }

    public TradeFinance createDocumentaryCollection(Long customerId, Long bankId, BigDecimal amount,
                                                  String currencyCode, TradeFinance.CollectionType collectionType,
                                                  String drawerName, String draweeName) {
        
        User customer = userRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        TradeFinance collection = new TradeFinance();
        collection.setBank(bank);
        collection.setCustomer(customer);
        collection.setReferenceNumber(generateReferenceNumber("DC"));
        collection.setTradeType(TradeFinance.TradeType.EXPORT);
        collection.setInstrumentType(TradeFinance.InstrumentType.DOCUMENTARY_COLLECTION);
        collection.setAmount(amount);
        collection.setCurrencyCode(currencyCode);
        collection.setStatus(TradeFinance.TradeStatus.DRAFT);
        collection.setCollectionType(collectionType);
        collection.setDrawerName(drawerName);
        collection.setDraweeName(draweeName);
        collection.setIssueDate(LocalDateTime.now());
        collection.setCountryRiskRating(TradeFinance.RiskRating.MEDIUM);
        collection.setCustomerRiskRating(TradeFinance.RiskRating.LOW);
        collection.setSanctionsScreeningStatus(TradeFinance.ScreeningStatus.PENDING);
        collection.setAmlScreeningStatus(TradeFinance.ScreeningStatus.PENDING);
        
        // Calculate charges
        calculateCollectionCharges(collection);
        
        collection = tradeFinanceRepository.save(collection);
        
        return collection;
    }

    public TradeFinance approveTradeFinance(Long tradeFinanceId, Long approvedBy) {
        TradeFinance tradeFinance = tradeFinanceRepository.findById(tradeFinanceId)
            .orElseThrow(() -> new RuntimeException("Trade finance not found"));
        
        tradeFinance.setStatus(TradeFinance.TradeStatus.APPROVED);
        tradeFinance.setApprovedBy(approvedBy);
        tradeFinance.setApprovalDate(LocalDateTime.now());
        
        // Perform sanctions and AML screening
        performComplianceScreening(tradeFinance);
        
        return tradeFinanceRepository.save(tradeFinance);
    }

    public TradeFinance issueInstrument(Long tradeFinanceId) {
        TradeFinance tradeFinance = tradeFinanceRepository.findById(tradeFinanceId)
            .orElseThrow(() -> new RuntimeException("Trade finance not found"));
        
        if (tradeFinance.getStatus() != TradeFinance.TradeStatus.APPROVED) {
            throw new RuntimeException("Trade finance must be approved before issuance");
        }
        
        tradeFinance.setStatus(TradeFinance.TradeStatus.ISSUED);
        
        // Generate SWIFT message
        generateSwiftMessage(tradeFinance);
        
        // Send notifications
        sendIssuanceNotifications(tradeFinance);
        
        return tradeFinanceRepository.save(tradeFinance);
    }

    public TradeDocument addDocument(Long tradeFinanceId, TradeDocument.DocumentType documentType,
                                   String documentName, String filePath) {
        
        TradeFinance tradeFinance = tradeFinanceRepository.findById(tradeFinanceId)
            .orElseThrow(() -> new RuntimeException("Trade finance not found"));
        
        TradeDocument document = new TradeDocument();
        document.setTradeFinance(tradeFinance);
        document.setDocumentType(documentType);
        document.setDocumentName(documentName);
        document.setFilePath(filePath);
        document.setIsReceived(true);
        document.setReceivedDate(LocalDateTime.now());
        document.setStatus(TradeDocument.DocumentStatus.RECEIVED);
        
        return tradeDocumentRepository.save(document);
    }

    public TradeDocument reviewDocument(Long documentId, TradeDocument.DocumentStatus status, 
                                      String discrepancies, Long reviewedBy) {
        
        TradeDocument document = tradeDocumentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        
        document.setStatus(status);
        document.setDiscrepancies(discrepancies);
        document.setReviewedBy(reviewedBy);
        document.setReviewDate(LocalDateTime.now());
        
        // Check if all documents are reviewed
        checkDocumentCompleteness(document.getTradeFinance());
        
        return tradeDocumentRepository.save(document);
    }

    public TradeAmendment requestAmendment(Long tradeFinanceId, TradeAmendment.AmendmentType amendmentType,
                                         String fieldChanged, String oldValue, String newValue, 
                                         String reason, Long requestedBy) {
        
        TradeFinance tradeFinance = tradeFinanceRepository.findById(tradeFinanceId)
            .orElseThrow(() -> new RuntimeException("Trade finance not found"));
        
        // Get next amendment number
        Integer nextAmendmentNumber = tradeAmendmentRepository
            .countByTradeFinance(tradeFinance).intValue() + 1;
        
        TradeAmendment amendment = new TradeAmendment();
        amendment.setTradeFinance(tradeFinance);
        amendment.setAmendmentNumber(nextAmendmentNumber);
        amendment.setAmendmentType(amendmentType);
        amendment.setFieldChanged(fieldChanged);
        amendment.setOldValue(oldValue);
        amendment.setNewValue(newValue);
        amendment.setReason(reason);
        amendment.setRequestedBy(requestedBy);
        amendment.setRequestDate(LocalDateTime.now());
        amendment.setStatus(TradeAmendment.AmendmentStatus.PENDING);
        
        // Calculate amendment charges
        calculateAmendmentCharges(amendment);
        
        amendment = tradeAmendmentRepository.save(amendment);
        
        // Start amendment approval workflow
        try {
            workflowEngineService.startWorkflow("AMENDMENT_APPROVAL", "TRADE_AMENDMENT", 
                amendment.getId(), requestedBy, null);
        } catch (Exception e) {
            // Log error but don't fail creation
        }
        
        return amendment;
    }

    public TradeAmendment approveAmendment(Long amendmentId, Long approvedBy) {
        TradeAmendment amendment = tradeAmendmentRepository.findById(amendmentId)
            .orElseThrow(() -> new RuntimeException("Amendment not found"));
        
        amendment.setStatus(TradeAmendment.AmendmentStatus.APPROVED);
        amendment.setApprovedBy(approvedBy);
        amendment.setApprovalDate(LocalDateTime.now());
        
        // Apply amendment to trade finance
        applyAmendment(amendment);
        
        // Generate SWIFT amendment message
        generateAmendmentSwiftMessage(amendment);
        
        return tradeAmendmentRepository.save(amendment);
    }

    private void calculateLCCharges(TradeFinance lc) {
        // Standard LC charges: 0.125% per quarter
        BigDecimal quarterlyRate = new BigDecimal("0.00125");
        long quarters = java.time.temporal.ChronoUnit.DAYS.between(
            lc.getIssueDate().toLocalDate(), 
            lc.getExpiryDate().toLocalDate()) / 90 + 1;
        
        lc.setCommissionRate(quarterlyRate.multiply(new BigDecimal(quarters)));
        lc.setCommissionAmount(lc.getAmount().multiply(lc.getCommissionRate()));
        
        // Standard charges
        lc.setCharges(new BigDecimal("500")); // Fixed charges
        
        // Margin requirement (10% for standard customers)
        lc.setMarginPercentage(new BigDecimal("10"));
        lc.setMarginAmount(lc.getAmount().multiply(lc.getMarginPercentage()).divide(new BigDecimal("100")));
    }

    private void calculateGuaranteeCharges(TradeFinance guarantee) {
        // Guarantee charges: 0.5% per annum
        BigDecimal annualRate = new BigDecimal("0.005");
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            guarantee.getIssueDate().toLocalDate(), 
            guarantee.getExpiryDate().toLocalDate());
        
        BigDecimal dailyRate = annualRate.divide(new BigDecimal("365"), 6, java.math.RoundingMode.HALF_UP);
        guarantee.setCommissionRate(dailyRate.multiply(new BigDecimal(days)));
        guarantee.setCommissionAmount(guarantee.getAmount().multiply(guarantee.getCommissionRate()));
        
        // Standard charges
        guarantee.setCharges(new BigDecimal("300"));
        
        // Margin requirement (5% for guarantees)
        guarantee.setMarginPercentage(new BigDecimal("5"));
        guarantee.setMarginAmount(guarantee.getAmount().multiply(guarantee.getMarginPercentage()).divide(new BigDecimal("100")));
    }

    private void calculateCollectionCharges(TradeFinance collection) {
        // Collection charges: 0.1% minimum $100
        BigDecimal rate = new BigDecimal("0.001");
        BigDecimal calculatedCharge = collection.getAmount().multiply(rate);
        BigDecimal minimumCharge = new BigDecimal("100");
        
        collection.setCommissionAmount(calculatedCharge.max(minimumCharge));
        collection.setCharges(new BigDecimal("50")); // Handling charges
    }

    private void calculateAmendmentCharges(TradeAmendment amendment) {
        // Standard amendment charges
        amendment.setCharges(new BigDecimal("150"));
    }

    private void performComplianceScreening(TradeFinance tradeFinance) {
        // Simulate sanctions screening
        if (isOnSanctionsList(tradeFinance.getBeneficiaryName())) {
            tradeFinance.setSanctionsScreeningStatus(TradeFinance.ScreeningStatus.FLAGGED);
        } else {
            tradeFinance.setSanctionsScreeningStatus(TradeFinance.ScreeningStatus.CLEARED);
        }
        
        // Simulate AML screening
        if (isHighRiskCountry(tradeFinance.getBeneficiaryBank())) {
            tradeFinance.setAmlScreeningStatus(TradeFinance.ScreeningStatus.FLAGGED);
        } else {
            tradeFinance.setAmlScreeningStatus(TradeFinance.ScreeningStatus.CLEARED);
        }
    }

    private boolean isOnSanctionsList(String name) {
        // Simulate sanctions list check
        return name != null && name.toLowerCase().contains("sanctioned");
    }

    private boolean isHighRiskCountry(String bank) {
        // Simulate high-risk country check
        return bank != null && (bank.toLowerCase().contains("iran") || 
                               bank.toLowerCase().contains("north korea"));
    }

    private void generateSwiftMessage(TradeFinance tradeFinance) {
        String messageType;
        switch (tradeFinance.getInstrumentType()) {
            case LETTER_OF_CREDIT:
                messageType = "MT700";
                break;
            case GUARANTEE:
                messageType = "MT760";
                break;
            case DOCUMENTARY_COLLECTION:
                messageType = "MT400";
                break;
            default:
                messageType = "MT999";
        }
        
        tradeFinance.setSwiftMessageType(messageType);
        tradeFinance.setSwiftReference(generateSwiftReference());
        tradeFinance.setSwiftSentDate(LocalDateTime.now());
    }

    private void generateAmendmentSwiftMessage(TradeAmendment amendment) {
        String messageType;
        switch (amendment.getTradeFinance().getInstrumentType()) {
            case LETTER_OF_CREDIT:
                messageType = "MT707";
                break;
            case GUARANTEE:
                messageType = "MT767";
                break;
            default:
                messageType = "MT999";
        }
        
        amendment.setSwiftMessageSent(true);
    }

    private void applyAmendment(TradeAmendment amendment) {
        TradeFinance tradeFinance = amendment.getTradeFinance();
        
        switch (amendment.getFieldChanged()) {
            case "amount":
                tradeFinance.setAmount(new BigDecimal(amendment.getNewValue()));
                break;
            case "expiryDate":
                tradeFinance.setExpiryDate(LocalDateTime.parse(amendment.getNewValue()));
                break;
            case "beneficiaryName":
                tradeFinance.setBeneficiaryName(amendment.getNewValue());
                break;
            // Add more fields as needed
        }
        
        tradeFinanceRepository.save(tradeFinance);
    }

    private void checkDocumentCompleteness(TradeFinance tradeFinance) {
        List<TradeDocument> documents = tradeDocumentRepository.findByTradeFinance(tradeFinance);
        
        boolean allDocumentsAccepted = documents.stream()
            .allMatch(doc -> doc.getStatus() == TradeDocument.DocumentStatus.ACCEPTED);
        
        if (allDocumentsAccepted && !documents.isEmpty()) {
            tradeFinance.setStatus(TradeFinance.TradeStatus.DOCUMENTS_ACCEPTED);
            tradeFinanceRepository.save(tradeFinance);
        }
    }

    private void sendIssuanceNotifications(TradeFinance tradeFinance) {
        try {
            notificationService.sendNotification(tradeFinance.getCustomer().getId(), 
                "TRADE_FINANCE_ISSUED", 
                "Your " + tradeFinance.getInstrumentType() + " has been issued");
        } catch (Exception e) {
            // Log error but don't fail
        }
    }

    private String generateReferenceNumber(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    private String generateLCNumber() {
        return "LC" + System.currentTimeMillis();
    }

    private String generateSwiftReference() {
        return "SWIFT" + System.currentTimeMillis();
    }

    public List<TradeFinance> getTradeFinanceByCustomer(Long customerId) {
        return tradeFinanceRepository.findByCustomerId(customerId);
    }

    public List<TradeFinance> getExpiringInstruments(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().plusDays(days);
        return tradeFinanceRepository.findByExpiryDateBeforeAndStatusIn(
            cutoffDate, 
            List.of(TradeFinance.TradeStatus.ISSUED, TradeFinance.TradeStatus.ADVISED)
        );
    }

    public Map<String, Object> getTradeFinanceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalInstruments", tradeFinanceRepository.count());
        stats.put("activeInstruments", tradeFinanceRepository.countByStatusIn(
            List.of(TradeFinance.TradeStatus.ISSUED, TradeFinance.TradeStatus.ADVISED)));
        stats.put("pendingApproval", tradeFinanceRepository.countByStatus(TradeFinance.TradeStatus.UNDER_REVIEW));
        stats.put("totalExposure", tradeFinanceRepository.getTotalExposure());
        
        return stats;
    }
}

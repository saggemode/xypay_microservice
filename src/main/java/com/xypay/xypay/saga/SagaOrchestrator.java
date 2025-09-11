package com.xypay.xypay.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypay.xypay.domain.Loan;
import com.xypay.xypay.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SagaOrchestrator {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SagaManager sagaManager;
    
    @Autowired
    private LoanService loanService;
    
    // Kafka listener removed for now
    // Will be added back later
    /*
    @KafkaListener(topics = "saga-events", groupId = "saga-orchestrator")
    public void handleSagaEvent(String stepJson) {
        try {
            SagaStep step = objectMapper.readValue(stepJson, SagaStep.class);
            
            switch (step.getSagaType()) {
                case "ACCOUNT_TRANSFER":
                    handleAccountTransferSaga(step);
                    break;
                case "LOAN_APPLICATION":
                    handleLoanApplicationSaga(step);
                    break;
                case "CUSTOMER_ONBOARDING":
                    handleCustomerOnboardingSaga(step);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
    
    private void handleAccountTransferSaga(SagaStep step) {
        // Implementation for account transfer saga
        System.out.println("Handling account transfer saga step: " + step.getStep());
        
        // Based on the current step, determine the next step
        switch (step.getStep()) {
            case "START":
                // Start the saga by debiting the source account
                sagaManager.continueSaga("ACCOUNT_TRANSFER", "DEBIT_SOURCE", step.getPayload(), "IN_PROGRESS");
                break;
            case "DEBIT_SOURCE":
                // After debiting source, credit the destination account
                sagaManager.continueSaga("ACCOUNT_TRANSFER", "CREDIT_DESTINATION", step.getPayload(), "IN_PROGRESS");
                break;
            case "CREDIT_DESTINATION":
                // Complete the saga
                sagaManager.continueSaga("ACCOUNT_TRANSFER", "COMPLETE", step.getPayload(), "COMPLETED");
                break;
            default:
                System.out.println("Unknown step in account transfer saga: " + step.getStep());
        }
    }
    
    private void handleLoanDisbursementSaga(SagaStep step) {
        // Implementation for loan disbursement saga
        System.out.println("Handling loan disbursement saga step: " + step.getStep());
        
        try {
            switch (step.getStep()) {
                case "START":
                    // Create loan account
                    sagaManager.continueSaga("LOAN_DISBURSEMENT", "CREATE_LOAN_ACCOUNT", step.getPayload(), "IN_PROGRESS");
                    break;
                case "CREATE_LOAN_ACCOUNT":
                    // Disburse funds to loan account
                    sagaManager.continueSaga("LOAN_DISBURSEMENT", "DISBURSE_FUNDS", step.getPayload(), "IN_PROGRESS");
                    break;
                case "DISBURSE_FUNDS":
                    // Update loan status to disbursed
                    Loan loan = objectMapper.readValue(step.getPayload(), Loan.class);
                    loanService.disburseLoan(loan.getId());
                    sagaManager.continueSaga("LOAN_DISBURSEMENT", "COMPLETE", step.getPayload(), "COMPLETED");
                    break;
                case "COMPLETE":
                    System.out.println("Loan disbursement saga completed");
                    break;
                default:
                    System.out.println("Unknown step in loan disbursement saga: " + step.getStep());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // In a real implementation, we would implement compensating transactions here
        }
    }
}
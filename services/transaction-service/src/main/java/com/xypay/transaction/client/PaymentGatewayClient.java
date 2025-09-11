package com.xypay.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "payment-gateway-service", url = "http://localhost:8085")
public interface PaymentGatewayClient {
    
    @PostMapping("/api/payments/transfer")
    TransferResponse processTransfer(@RequestBody TransferRequest request);
    
    @PostMapping("/api/payments/card")
    CardPaymentResponse processCardPayment(@RequestBody CardPaymentRequest request);
    
    @PostMapping("/api/payments/validate-account")
    AccountValidationResponse validateAccount(@RequestBody AccountValidationRequest request);
    
    class TransferRequest {
        public String sourceAccountNumber;
        public String destinationBank;
        public String destinationAccountNumber;
        public String destinationAccountName;
        public BigDecimal amount;
        public String currency;
        public String reference;
        public String routingNumber;
        
        public TransferRequest() {}
        
        public TransferRequest(String sourceAccountNumber, String destinationBank, 
                             String destinationAccountNumber, String destinationAccountName,
                             BigDecimal amount, String currency, String reference, String routingNumber) {
            this.sourceAccountNumber = sourceAccountNumber;
            this.destinationBank = destinationBank;
            this.destinationAccountNumber = destinationAccountNumber;
            this.destinationAccountName = destinationAccountName;
            this.amount = amount;
            this.currency = currency;
            this.reference = reference;
            this.routingNumber = routingNumber;
        }
    }
    
    class TransferResponse {
        public boolean success;
        public String gatewayTransactionId;
        public String status;
        public String errorMessage;
        public String processingTime;
        
        public TransferResponse() {}
        
        public boolean isSuccess() { return success; }
        public String getGatewayTransactionId() { return gatewayTransactionId; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    class CardPaymentRequest {
        public String cardNumber;
        public String cardHolderName;
        public String expiryDate;
        public String cvv;
        public BigDecimal amount;
        public String currency;
        public String reference;
        public String description;
        
        public CardPaymentRequest() {}
        
        public CardPaymentRequest(String cardNumber, String cardHolderName, String expiryDate,
                                String cvv, BigDecimal amount, String currency, String reference, String description) {
            this.cardNumber = cardNumber;
            this.cardHolderName = cardHolderName;
            this.expiryDate = expiryDate;
            this.cvv = cvv;
            this.amount = amount;
            this.currency = currency;
            this.reference = reference;
            this.description = description;
        }
    }
    
    class CardPaymentResponse {
        public boolean success;
        public String gatewayTransactionId;
        public String status;
        public String errorMessage;
        public String authorizationCode;
        
        public CardPaymentResponse() {}
        
        public boolean isSuccess() { return success; }
        public String getGatewayTransactionId() { return gatewayTransactionId; }
        public String getErrorMessage() { return errorMessage; }
        public String getAuthorizationCode() { return authorizationCode; }
    }
    
    class AccountValidationRequest {
        public String bankCode;
        public String accountNumber;
        public String accountName;
        
        public AccountValidationRequest() {}
        
        public AccountValidationRequest(String bankCode, String accountNumber, String accountName) {
            this.bankCode = bankCode;
            this.accountNumber = accountNumber;
            this.accountName = accountName;
        }
    }
    
    class AccountValidationResponse {
        public boolean valid;
        public String accountName;
        public String bankName;
        public String errorMessage;
        
        public AccountValidationResponse() {}
        
        public boolean isValid() { return valid; }
        public String getAccountName() { return accountName; }
        public String getBankName() { return bankName; }
        public String getErrorMessage() { return errorMessage; }
    }
}

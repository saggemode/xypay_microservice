package com.xypay.xypay.service;

import com.xypay.xypay.domain.Account;
import com.xypay.xypay.domain.Customer;
import com.xypay.xypay.domain.Complaint;
import com.xypay.xypay.domain.KYCDocument;
import com.xypay.xypay.repository.AccountRepository;
import com.xypay.xypay.repository.CustomerRepository;
import com.xypay.xypay.repository.ComplaintRepository;
import com.xypay.xypay.repository.KYCDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CSOService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ComplaintRepository complaintRepository;
    @Autowired
    private KYCDocumentRepository kycDocumentRepository;
    @Autowired
    private AccountService accountService;

    public String openAccount(String customerName, String accountType, String currency, String legalId, String kycStatus, String firstName, String lastName) {
        Customer customer = customerRepository.findAll().stream()
                .filter(c -> c.getName().equals(customerName))
                .findFirst()
                .orElse(null);
        if (customer == null) {
            customer = new Customer();
            customer.setCustomerNumber("CUST-" + java.util.UUID.randomUUID().toString().substring(0, 8));
            customer.setName(customerName);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setLegalId(legalId);
            customer.setKycStatus(kycStatus);
            customer.setCreatedAt(java.time.LocalDateTime.now());
            customerRepository.save(customer);
        }
        Account account = accountService.createAccount(customer.getId(), currency, accountType);
        return "Account opened successfully. Account Number: " + account.getAccountNumber();
    }

    public String submitComplaint(String customerName, String complaintType, String description) {
        Customer customer = customerRepository.findAll().stream()
                .filter(c -> c.getName().equals(customerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        Complaint complaint = new Complaint();
        complaint.setCustomerId(customer.getId());
        complaint.setComplaintType(complaintType);
        complaint.setDescription(description);
        complaint.setStatus("OPEN");
        complaintRepository.save(complaint);
        return "Complaint submitted successfully. Complaint ID: " + complaint.getId();
    }

    public String verifyKYC(String customerName, String kycDocument) {
        // For demo, just return a success message. In real app, update KYC status in Customer.
        return "KYC verified for " + customerName + ".";
    }

    public List<Customer> searchCustomers(String name) {
        return customerRepository.findAll().stream()
            .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(name.toLowerCase()))
            .toList();
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public String updateCustomer(Long customerId, String name) {
        Customer customer = getCustomerById(customerId);
        customer.setName(name);
        customerRepository.save(customer);
        return "Customer updated successfully.";
    }

    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public void updateComplaintStatus(Long id, String status) {
        complaintRepository.findById(id).ifPresent(c -> {
            c.setStatus(status);
            complaintRepository.save(c);
        });
    }

    public Complaint submitComplaintEntity(String customerName, String complaint) {
        Complaint c = new Complaint();
        c.setCustomerName(customerName);
        c.setDescription(complaint);
        c.setStatus("OPEN");
        return complaintRepository.save(c);
    }

    public List<KYCDocument> getAllKYCDocuments() {
        return kycDocumentRepository.findAll();
    }

    public void updateKYCDocumentStatus(Long id, String status) {
        kycDocumentRepository.findById(id).ifPresent(doc -> {
            doc.setStatus(status);
            kycDocumentRepository.save(doc);
        });
    }

    public KYCDocument uploadKYCDocument(String customerName, String fileName) {
        KYCDocument doc = new KYCDocument();
        doc.setCustomerName(customerName);
        doc.setFileName(fileName);
        doc.setStatus("PENDING");
        return kycDocumentRepository.save(doc);
    }
}

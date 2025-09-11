package com.xypay.xypay.controller;

import com.xypay.xypay.service.CSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.xypay.xypay.domain.Complaint;

import com.xypay.xypay.domain.Customer;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CSOController {
    @Autowired
    private CSOService csoService;

    @GetMapping("/cso/account-opening")
    public String accountOpeningForm() {
        return "cso-account-opening";
    }

    @PostMapping("/cso/account-opening")
    public String openAccount(@RequestParam String customerName,
                             @RequestParam String accountType,
                             @RequestParam String currency,
                             @RequestParam String legalId,
                             @RequestParam String kycStatus,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             Model model) {
        String result = csoService.openAccount(customerName, accountType, currency, legalId, kycStatus, firstName, lastName);
        model.addAttribute("result", result);
        return "cso-account-opening";
    }

    @GetMapping("/cso/complaints")
    public String complaintsForm() {
        return "cso-complaints";
    }

    @PostMapping("/cso/customer-search")
    public String searchCustomer(@RequestParam String customerName, Model model) {
        Customer customer = csoService.searchCustomers(customerName).stream()
                .findFirst()
                .orElse(null);
        model.addAttribute("customer", customer);
        return "cso-crm";
    }

    @PostMapping("/cso/customer-edit")
    public String editCustomer(@RequestParam Long customerId, @RequestParam String name, Model model) {
        String result = csoService.updateCustomer(customerId, name);
        model.addAttribute("result", result);
        return "cso-crm";
    }

    @GetMapping("/cso/complaints-tracking")
    public String complaintsTracking(Model model) {
        model.addAttribute("complaints", csoService.getAllComplaints());
        return "cso-complaints-tracking";
    }

    @PostMapping("/cso/complaints-tracking/{id}/status")
    public String updateComplaintStatus(@PathVariable Long id, @RequestParam String status, Model model) {
        csoService.updateComplaintStatus(id, status);
        model.addAttribute("complaints", csoService.getAllComplaints());
        model.addAttribute("result", "Complaint status updated.");
        return "cso-complaints-tracking";
    }

    @PostMapping("/cso/complaints")
    public String submitComplaint(@RequestParam String customerName, @RequestParam String complaint, Model model) {
        Complaint c = csoService.submitComplaintEntity(customerName, complaint);
        model.addAttribute("result", "Complaint submitted for " + customerName + ".");
        return "cso-complaints";
    }

    @GetMapping("/cso/kyc")
    public String kycForm() {
        return "cso-kyc";
    }

    @PostMapping("/cso/kyc")
    public String verifyKYC(@RequestParam String customerName, @RequestParam(required = false) MultipartFile kycDocument, Model model) {
        String result = csoService.verifyKYC(customerName, kycDocument != null ? kycDocument.getOriginalFilename() : "");
        model.addAttribute("result", result);
        return "cso-kyc";
    }

    @GetMapping("/cso/kyc-documents")
    public String kycDocuments(Model model) {
        model.addAttribute("kycDocuments", csoService.getAllKYCDocuments());
        return "cso-kyc-documents";
    }

    @PostMapping("/cso/kyc-documents/upload")
    public String uploadKYCDocument(@RequestParam String customerName, @RequestParam MultipartFile kycDocument, Model model) {
        csoService.uploadKYCDocument(customerName, kycDocument.getOriginalFilename());
        model.addAttribute("kycDocuments", csoService.getAllKYCDocuments());
        model.addAttribute("result", "KYC document uploaded for " + customerName + ".");
        return "cso-kyc-documents";
    }

    @PostMapping("/cso/kyc-documents/{id}/status")
    public String updateKYCDocumentStatus(@PathVariable Long id, @RequestParam String status, Model model) {
        csoService.updateKYCDocumentStatus(id, status);
        model.addAttribute("kycDocuments", csoService.getAllKYCDocuments());
        model.addAttribute("result", "KYC document status updated.");
        return "cso-kyc-documents";
    }

    @GetMapping("/cso/crm")
    public String crmForm() {
        return "cso-crm";
    }

    @GetMapping("/cso/dashboard")
    public String csoDashboard() {
        return "cso-dashboard";
    }

    @GetMapping("/cso/user-registrations")
    public String userRegistrationsMonitor() {
        return "cso-user-registrations";
    }
}

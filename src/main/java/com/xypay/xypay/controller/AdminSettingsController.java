package com.xypay.xypay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminSettingsController {


    @GetMapping("/settings")
    public String adminSettings(Model model) {
        // Add any settings data to the model
        model.addAttribute("pageTitle", "Admin Settings");
        return "admin/settings";
    }

    // XySave Account Management
    @GetMapping("/xysave")
    public String xySaveManagement(Model model) {
        model.addAttribute("pageTitle", "XySave Account Management");
        return "admin/xysave";
    }

    @GetMapping("/spend-save")
    public String spendSaveManagement(Model model) {
        model.addAttribute("pageTitle", "Spend & Save Management");
        return "admin/spend-save";
    }

    @GetMapping("/target-savings")
    public String targetSavingsManagement(Model model) {
        model.addAttribute("pageTitle", "Target Savings Management");
        return "admin/target-savings";
    }

    @GetMapping("/fixed-savings")
    public String fixedSavingsManagement(Model model) {
        model.addAttribute("pageTitle", "Fixed Savings Management");
        return "admin/fixed-savings";
    }

    // Security & Fraud Prevention
    @GetMapping("/night-guard")
    public String nightGuardSettings(Model model) {
        model.addAttribute("pageTitle", "Night Guard Settings");
        return "admin/night-guard";
    }

    @GetMapping("/transaction-shield")
    public String transactionShieldSettings(Model model) {
        model.addAttribute("pageTitle", "Large Transaction Shield");
        return "admin/transaction-shield";
    }

    @GetMapping("/location-guard")
    public String locationGuardSettings(Model model) {
        model.addAttribute("pageTitle", "Location Guard Settings");
        return "admin/location-guard";
    }

    @GetMapping("/fraud-detection-settings")
    public String fraudDetectionSettings(Model model) {
        model.addAttribute("pageTitle", "Fraud Detection Management");
        return "admin/fraud-detection-settings";
    }

    // Staff Management
    @GetMapping("/staff-roles")
    public String staffRolesManagement(Model model) {
        model.addAttribute("pageTitle", "Staff Roles & Profiles");
        return "admin/staff-roles";
    }

    @GetMapping("/transaction-approvals")
    public String transactionApprovalsManagement(Model model) {
        model.addAttribute("pageTitle", "Transaction Approvals");
        return "admin/transaction-approvals";
    }

    @GetMapping("/customer-escalations")
    public String customerEscalationsManagement(Model model) {
        model.addAttribute("pageTitle", "Customer Escalations");
        return "admin/customer-escalations";
    }

    @GetMapping("/staff-activity")
    public String staffActivityTracking(Model model) {
        model.addAttribute("pageTitle", "Staff Activity Tracking");
        return "admin/staff-activity";
    }

    // Advanced Transaction Features
    @GetMapping("/transaction-reversals")
    public String transactionReversalsManagement(Model model) {
        model.addAttribute("pageTitle", "Transaction Reversals");
        return "admin/transaction-reversals";
    }

    @GetMapping("/transaction-metadata")
    public String transactionMetadataManagement(Model model) {
        model.addAttribute("pageTitle", "Transaction Metadata & Filtering");
        return "admin/transaction-metadata";
    }

    @GetMapping("/bulk-actions")
    public String bulkActionsManagement(Model model) {
        model.addAttribute("pageTitle", "Bulk Actions Management");
        return "admin/bulk-actions";
    }

    @GetMapping("/export-statements")
    public String exportStatementsManagement(Model model) {
        model.addAttribute("pageTitle", "Export Options");
        return "admin/export-statements";
    }

    // Interest & Merchant Features
    @GetMapping("/interest-engine")
    public String interestEngineManagement(Model model) {
        model.addAttribute("pageTitle", "Interest Calculation Engine");
        return "admin/interest-engine";
    }

    @GetMapping("/interest-breakdown")
    public String interestBreakdownManagement(Model model) {
        model.addAttribute("pageTitle", "Interest Breakdown");
        return "admin/interest-breakdown";
    }

    @GetMapping("/merchant-settlement")
    public String merchantSettlementManagement(Model model) {
        model.addAttribute("pageTitle", "Merchant Settlement");
        return "admin/merchant-settlement";
    }

    @GetMapping("/merchant-verification")
    public String merchantVerificationManagement(Model model) {
        model.addAttribute("pageTitle", "Merchant Verification");
        return "admin/merchant-verification";
    }

    // Charge Control Management
    @GetMapping("/charge-control")
    public String chargeControlManagement(Model model) {
        model.addAttribute("pageTitle", "Charge Control Management");
        return "admin/charge-control";
    }
}

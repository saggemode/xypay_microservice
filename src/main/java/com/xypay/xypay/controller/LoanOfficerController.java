package com.xypay.xypay.controller;

import com.xypay.xypay.service.LoanOfficerService;
import com.xypay.xypay.domain.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@Controller
public class LoanOfficerController {
    @Autowired
    private LoanOfficerService loanOfficerService;

    @GetMapping("/loan-officer/application")
    public String loanApplicationForm() {
        return "loan-officer-application";
    }

    @PostMapping("/loan-officer/application")
    public String applyForLoan(@RequestParam Long customerId, @RequestParam Long productId, @RequestParam String amount, Model model) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID customerIdUuid = new UUID(0L, customerId); // Create UUID from Long
        UUID productIdUuid = new UUID(0L, productId); // Create UUID from Long
        String result = loanOfficerService.applyForLoan(customerIdUuid, productIdUuid, new java.math.BigDecimal(amount));
        model.addAttribute("result", result);
        return "loan-officer-application";
    }

    @GetMapping("/loan-officer/credit-check")
    public String creditCheckForm() {
        return "loan-officer-credit-check";
    }

    @PostMapping("/loan-officer/credit-check")
    public String creditCheck(@RequestParam Long customerId, Model model) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID customerIdUuid = new UUID(0L, customerId); // Create UUID from Long
        String result = loanOfficerService.creditCheck(customerIdUuid);
        model.addAttribute("result", result);
        return "loan-officer-credit-check";
    }

    @GetMapping("/loan-officer/portfolio")
    public String portfolio(Model model) {
        java.util.List<Loan> loans = loanOfficerService.getPortfolio();
        model.addAttribute("loans", loans);
        return "loan-officer-portfolio";
    }

    @PostMapping("/loan-officer/portfolio/{id}/approve")
    public String approveLoan(@PathVariable("id") Long id, Model model) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID loanIdUuid = new UUID(0L, id); // Create UUID from Long
        loanOfficerService.approveLoan(loanIdUuid);
        model.addAttribute("loans", loanOfficerService.getPortfolio());
        model.addAttribute("result", "Loan approved.");
        return "loan-officer-portfolio";
    }

    @PostMapping("/loan-officer/portfolio/{id}/reject")
    public String rejectLoan(@PathVariable("id") Long id, Model model) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID loanIdUuid = new UUID(0L, id); // Create UUID from Long
        loanOfficerService.rejectLoan(loanIdUuid);
        model.addAttribute("loans", loanOfficerService.getPortfolio());
        model.addAttribute("result", "Loan rejected.");
        return "loan-officer-portfolio";
    }

    @GetMapping("/loan-officer/portfolio/{id}/schedule")
    public String repaymentSchedule(@PathVariable("id") Long id, Model model) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID loanIdUuid = new UUID(0L, id); // Create UUID from Long
        model.addAttribute("schedule", loanOfficerService.getRepaymentSchedule(loanIdUuid));
        return "loan-officer-repayment-schedule";
    }

    @GetMapping("/loan-officer/risk-assessment")
    public String riskAssessmentForm() {
        return "loan-officer-risk-assessment";
    }

    @PostMapping("/loan-officer/risk-assessment")
    public String riskAssessment(@RequestParam Long customerId, Model model) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID customerIdUuid = new UUID(0L, customerId); // Create UUID from Long
        String result = loanOfficerService.riskAssessment(customerIdUuid);
        model.addAttribute("result", result);
        return "loan-officer-risk-assessment";
    }

    @GetMapping("/loan-officer/credit-score")
    public String creditScore(@RequestParam String customerName, Model model) {
        int score = loanOfficerService.getCreditScore(customerName);
        model.addAttribute("score", score);
        model.addAttribute("customerName", customerName);
        return "loan-officer-credit-score";
    }

    @GetMapping("/loan-officer/dashboard")
    public String loanOfficerDashboard() {
        return "loan-officer-dashboard";
    }
}

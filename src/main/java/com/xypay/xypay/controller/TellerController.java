package com.xypay.xypay.controller;

import com.xypay.xypay.service.TellerService;
import com.xypay.xypay.domain.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TellerController {
    @Autowired
    private TellerService tellerService;

    @GetMapping("/teller/deposit")
    public String depositForm() {
        return "teller-deposit";
    }

    @GetMapping("/teller/withdrawal")
    public String withdrawalForm() {
        return "teller-withdrawal";
    }

    @PostMapping("/teller/deposit")
    public String deposit(@RequestParam String accountNumber, @RequestParam String amount, Model model) {
        String result = tellerService.deposit(accountNumber, new java.math.BigDecimal(amount));
        model.addAttribute("result", result);
        return "teller-deposit";
    }

    @PostMapping("/teller/withdrawal")
    public String withdrawal(@RequestParam String accountNumber, @RequestParam String amount, Model model) {
        String result = tellerService.withdrawal(accountNumber, new java.math.BigDecimal(amount));
        model.addAttribute("result", result);
        return "teller-withdrawal";
    }

    @GetMapping("/teller/account-lookup")
    public String accountLookup(@RequestParam(required = false) String accountNumber, Model model) {
        if (accountNumber != null) {
            java.util.Optional<Wallet> wallet = tellerService.accountLookup(accountNumber);
            if (wallet.isPresent()) {
                model.addAttribute("account", wallet.get());
                model.addAttribute("balance", tellerService.getBalance(accountNumber));
                model.addAttribute("transactions", tellerService.getTransactionHistory(accountNumber));
            } else {
                model.addAttribute("error", "Account not found");
            }
        }
        return "teller-account-lookup";
    }

    @GetMapping("/teller/dashboard")
    public String tellerDashboard() {
        return "teller-dashboard";
    }

    @GetMapping("/teller/check-cashing")
    public String checkCashingForm() {
        return "teller-check-cashing";
    }

    @PostMapping("/teller/check-cashing")
    public String checkCashing(@RequestParam String checkNumber, @RequestParam String accountNumber, @RequestParam String amount, Model model) {
        String result = tellerService.checkCashing(checkNumber, accountNumber, new java.math.BigDecimal(amount));
        model.addAttribute("result", result);
        return "teller-check-cashing";
    }
}

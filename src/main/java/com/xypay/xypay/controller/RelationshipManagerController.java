package com.xypay.xypay.controller;

import com.xypay.xypay.service.RelationshipManagerService;
import com.xypay.xypay.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RelationshipManagerController {
    @Autowired
    private RelationshipManagerService rmService;

    @GetMapping("/rm/portfolio")
    public String portfolio(Model model) {
        java.util.List<Customer> customers = rmService.getPortfolio();
        model.addAttribute("customers", customers);
        return "rm-portfolio";
    }

    @GetMapping("/rm/dashboard")
    public String rmDashboard() {
        return "rm-dashboard";
    }

    @PostMapping("/rm/crm")
    public String crm(Model model) {
        String result = rmService.crmTools();
        model.addAttribute("result", result);
        return "rm-crm";
    }

    @PostMapping("/rm/product-sales")
    public String productSales(Model model) {
        String result = rmService.productSales();
        model.addAttribute("result", result);
        return "rm-product-sales";
    }

    @PostMapping("/rm/transaction-initiation")
    public String transactionInitiation(Model model) {
        String result = rmService.transactionInitiation();
        model.addAttribute("result", result);
        return "rm-transaction-initiation";
    }
}

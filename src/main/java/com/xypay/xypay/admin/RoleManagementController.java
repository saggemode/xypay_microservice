package com.xypay.xypay.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class RoleManagementController {
    
    @GetMapping("/roles")
    public String listRoles(Model model) {
        List<RoleInfo> roles = Arrays.asList(
            new RoleInfo("ROLE_SUPERUSER", "Superuser", "Complete system administration and control", "System Administration"),
            new RoleInfo("ROLE_ADMIN", "Admin", "Administrative functions and user management", "Administration"),
            new RoleInfo("ROLE_TELLER", "Teller", "Handle deposits, withdrawals, and transactions", "Frontline Banking"),
            new RoleInfo("ROLE_CUSTOMER_SERVICE_OFFICER", "Customer Service Officer", "Account opening, complaints, KYC", "Customer Service"),
            new RoleInfo("ROLE_LOAN_OFFICER", "Loan Officer", "Loan applications, approvals, credit checks", "Lending"),
            new RoleInfo("ROLE_RELATIONSHIP_MANAGER", "Relationship Manager", "VIP client management, portfolio management", "Relationship Banking"),
            new RoleInfo("ROLE_OPERATIONS_OFFICER", "Operations Officer", "Back-office operations and processing", "Operations"),
            new RoleInfo("ROLE_COMPLIANCE_OFFICER", "Compliance Officer", "Regulatory compliance and monitoring", "Compliance"),
            new RoleInfo("ROLE_TREASURY_OFFICER", "Treasury Officer", "Treasury operations and liquidity management", "Treasury"),
            new RoleInfo("ROLE_IT_SUPPORT", "IT Support", "Technical support and system maintenance", "IT Support"),
            new RoleInfo("ROLE_BRANCH_MANAGER", "Branch Manager", "Branch operations and staff management", "Branch Management"),
            new RoleInfo("ROLE_OPERATIONS_MANAGER", "Operations Manager", "Operations oversight and process management", "Operations Management"),
            new RoleInfo("ROLE_CHIEF_RISK_OFFICER", "Chief Risk Officer", "Risk management and oversight", "Risk Management"),
            new RoleInfo("ROLE_EXECUTIVE", "Executive", "Executive-level access and reporting", "Executive")
        );
        
        model.addAttribute("roles", roles);
        return "admin-roles";
    }
    
    public static class RoleInfo {
        private String code;
        private String name;
        private String description;
        private String category;
        
        public RoleInfo(String code, String name, String description, String category) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.category = category;
        }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}

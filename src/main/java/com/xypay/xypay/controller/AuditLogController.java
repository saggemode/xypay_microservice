package com.xypay.xypay.controller;

import com.xypay.xypay.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class AuditLogController {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @PreAuthorize("hasAnyRole('SUPERUSER', 'ADMIN')")
    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        model.addAttribute("logs", auditLogRepository.findAll());
        return "admin-audit-logs";
    }
}

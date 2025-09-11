package com.xypay.xypay.admin;

import com.xypay.xypay.domain.TransferChargeControl;
import com.xypay.xypay.repository.TransferChargeControlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/cbn-levies")
public class CbnLevyAdminController {

    @Autowired
    private TransferChargeControlRepository transferChargeControlRepository;

    @GetMapping
    public String cbnLevies(Model model) {
        TransferChargeControl control = transferChargeControlRepository.findFirstByOrderByUpdatedAtDesc().orElse(null);
        model.addAttribute("levyActive", control != null ? control.getLevyActive() : Boolean.TRUE);
        model.addAttribute("levyThreshold", "₦10,000 per block");
        model.addAttribute("levyAmount", "₦50 per block");
        return "admin/cbn-levies";
    }
}



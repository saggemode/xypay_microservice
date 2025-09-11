package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Branch;
import com.xypay.xypay.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    @Autowired
    private BranchService branchService;

    @PostMapping("/create")
    public ResponseEntity<Branch> createBranch(@RequestParam Long bankId, @RequestParam String name, @RequestParam String code, @RequestParam String address) {
        return ResponseEntity.ok(branchService.createBranch(bankId, name, code, address));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> getBranch(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.getBranchById(id));
    }

    @GetMapping
    public ResponseEntity<List<Branch>> listBranches() {
        return ResponseEntity.ok(branchService.listBranches());
    }

    @PostMapping("/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable Long id, @RequestParam boolean active) {
        branchService.setActive(id, active);
        return ResponseEntity.ok().build();
    }
}
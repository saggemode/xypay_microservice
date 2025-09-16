package com.xypay.xypay.admin;

import com.xypay.xypay.domain.TransferChargeControl;
import com.xypay.xypay.domain.VATCharge;
import com.xypay.xypay.repository.TransferChargeControlRepository;
import com.xypay.xypay.repository.VATChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/admin/charge-control")
public class ChargeControlAdminController {
    
    @Autowired
    private TransferChargeControlRepository transferChargeControlRepository;
    
    @Autowired
    private VATChargeRepository vatChargeRepository;
    
    // Transfer Charge Control Endpoints
    
    @PostMapping("/transfer-control")
    public ResponseEntity<TransferChargeControl> createTransferControl(
            @RequestBody TransferChargeControl control) {
        TransferChargeControl savedControl = transferChargeControlRepository.save(control);
        return ResponseEntity.ok(savedControl);
    }
    
    @GetMapping("/transfer-control")
    public ResponseEntity<TransferChargeControl> getLatestTransferControl() {
        Optional<TransferChargeControl> control = transferChargeControlRepository.findFirstByOrderByUpdatedAtDesc();
        return control.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/transfer-control/all")
    public ResponseEntity<List<TransferChargeControl>> getAllTransferControls() {
        List<TransferChargeControl> controls = transferChargeControlRepository.findAll();
        return ResponseEntity.ok(controls);
    }
    
    @PutMapping("/transfer-control/{id}")
    public ResponseEntity<TransferChargeControl> updateTransferControl(
            @PathVariable UUID id,
            @RequestBody TransferChargeControl control) {
        control.setId(id);
        TransferChargeControl updatedControl = transferChargeControlRepository.save(control);
        return ResponseEntity.ok(updatedControl);
    }
    
    // VAT Charge Endpoints
    
    @PostMapping("/vat")
    public ResponseEntity<VATCharge> createVatCharge(
            @RequestBody VATCharge vatCharge) {
        VATCharge savedVat = vatChargeRepository.save(vatCharge);
        return ResponseEntity.ok(savedVat);
    }
    
    @GetMapping("/vat")
    public ResponseEntity<VATCharge> getActiveVatCharge() {
        Optional<VATCharge> vat = vatChargeRepository.findFirstByActiveTrueOrderByUpdatedAtDesc();
        return vat.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/vat/all")
    public ResponseEntity<List<VATCharge>> getAllActiveVatCharges() {
        List<VATCharge> vats = vatChargeRepository.findByActiveTrue();
        return ResponseEntity.ok(vats);
    }
    
    @PutMapping("/vat/{id}")
    public ResponseEntity<VATCharge> updateVatCharge(
            @PathVariable UUID id,
            @RequestBody VATCharge vatCharge) {
        vatCharge.setId(id);
        VATCharge updatedVat = vatChargeRepository.save(vatCharge);
        return ResponseEntity.ok(updatedVat);
    }
}
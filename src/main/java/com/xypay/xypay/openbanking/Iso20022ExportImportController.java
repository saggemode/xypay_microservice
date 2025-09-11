package com.xypay.xypay.openbanking;

import com.xypay.xypay.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iso20022")
public class Iso20022ExportImportController {
    private final PaymentService paymentService;
    public Iso20022ExportImportController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_XML_VALUE)
    public String exportXml() {
        return paymentService.exportPaymentsAsIso20022Xml();
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> importXml(@RequestBody String xml) {
        int imported = paymentService.importPaymentsFromIso20022Xml(xml);
        return ResponseEntity.ok("Imported " + imported + " payments from ISO 20022 XML.");
    }
}

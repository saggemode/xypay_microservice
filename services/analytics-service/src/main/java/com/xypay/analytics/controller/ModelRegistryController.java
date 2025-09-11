package com.xypay.analytics.controller;

import com.xypay.analytics.domain.FraudRiskScore;
import com.xypay.analytics.domain.MLModel;
import com.xypay.analytics.repository.FraudRiskScoreRepository;
import com.xypay.analytics.repository.MLModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/models")
@RequiredArgsConstructor
public class ModelRegistryController {

    private final MLModelRepository modelRepository;
    private final FraudRiskScoreRepository fraudRiskScoreRepository;

    @PostMapping
    public ResponseEntity<MLModel> create(@RequestBody MLModel model) {
        model.setTrainingDate(model.getTrainingDate() == null ? LocalDateTime.now() : model.getTrainingDate());
        return ResponseEntity.ok(modelRepository.save(model));
    }

    @GetMapping
    public ResponseEntity<List<MLModel>> list() {
        return ResponseEntity.ok(modelRepository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MLModel> update(@PathVariable Long id, @RequestBody MLModel model) {
        return modelRepository.findById(id)
                .map(existing -> {
                    model.setId(existing.getId());
                    return ResponseEntity.ok(modelRepository.save(model));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (modelRepository.existsById(id)) {
            modelRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/score/fraud")
    public ResponseEntity<FraudRiskScore> scoreFraud(@RequestParam Long transactionId,
                                                     @RequestParam double riskScore) {
        FraudRiskScore s = new FraudRiskScore();
        s.setTransactionId(transactionId);
        s.setRiskScore(riskScore);
        s.setAssessmentDate(LocalDateTime.now());
        s.setRiskLevel(riskScore >= 0.7 ? "HIGH" : riskScore >= 0.4 ? "MEDIUM" : "LOW");
        s.setProcessed(false);
        return ResponseEntity.ok(fraudRiskScoreRepository.save(s));
    }
}



package com.xypay.xypay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final Random random = new Random();

    @PostMapping("/credit-score")
    public ResponseEntity<Map<String, Object>> creditScore(@RequestBody Map<String, Object> customer) {
        int score = 600 + random.nextInt(201); // 600-800
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("risk", score > 750 ? "Low" : score > 650 ? "Medium" : "High");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/fraud-detection")
    public ResponseEntity<Map<String, Object>> fraudDetection(@RequestBody Map<String, Object> transaction) {
        boolean suspicious = random.nextBoolean();
        Map<String, Object> result = new HashMap<>();
        result.put("suspicious", suspicious);
        result.put("reason", suspicious ? "Unusual location or amount" : "None");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/customer-segmentation")
    public ResponseEntity<Map<String, Object>> customerSegmentation(@RequestBody Map<String, Object> customer) {
        String[] segments = {"Mass Market", "Affluent", "SME", "Corporate", "VIP"};
        String segment = segments[random.nextInt(segments.length)];
        Map<String, Object> result = new HashMap<>();
        result.put("segment", segment);
        return ResponseEntity.ok(result);
    }
}

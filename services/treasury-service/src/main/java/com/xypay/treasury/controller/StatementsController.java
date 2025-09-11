package com.xypay.treasury.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/treasury/statements")
@RequiredArgsConstructor
@Slf4j
public class StatementsController {

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importCsv(@RequestParam Long bankAccountId,
                                            @RequestPart("file") MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            log.info("Imported CSV statement for account={} lines={} filename={}", bankAccountId, lines.size(), file.getOriginalFilename());
            // Persist bank_statement row and store raw content
            return ResponseEntity.ok("Imported " + lines.size() + " lines");
        }
    }
}



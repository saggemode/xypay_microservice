package com.xypay.analytics.controller;

import com.xypay.analytics.domain.DailyAggregate;
import com.xypay.analytics.repository.DailyAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@RestController
@RequestMapping("/api/analytics/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final DailyAggregateRepository dailyAggregateRepository;

    @GetMapping(value = "/daily.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportDailyCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<DailyAggregate> rows = dailyAggregateRepository.findByDateBetweenOrderByDateAsc(start, end);
        String header = "date,transactionsCount,transactionsVolume,activeUsers";
        String body = rows.stream()
                .map(r -> String.format("%s,%d,%s,%d",
                        r.getDate(),
                        r.getTransactionsCount() == null ? 0 : r.getTransactionsCount(),
                        r.getTransactionsVolume() == null ? "0" : r.getTransactionsVolume().toPlainString(),
                        r.getActiveUsers() == null ? 0 : r.getActiveUsers()))
                .collect(Collectors.joining("\n"));
        String csv = header + "\n" + body + "\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics-daily.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    // Daily report scheduler at 06:00
    @Scheduled(cron = "0 0 6 * * *")
    public void scheduledDailyReport() {
        // Placeholder: generate and email/upload report. Currently no-op.
    }

    @GetMapping(value = "/daily.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportDailyPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) throws Exception {
        List<DailyAggregate> rows = dailyAggregateRepository.findByDateBetweenOrderByDateAsc(start, end);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, baos);
        doc.open();
        doc.add(new Paragraph("Daily Analytics Report"));
        doc.add(new Paragraph("Range: " + start + " to " + end));
        Table table = new Table(4);
        table.addCell("date");
        table.addCell("transactionsCount");
        table.addCell("transactionsVolume");
        table.addCell("activeUsers");
        for (DailyAggregate r : rows) {
            table.addCell(String.valueOf(r.getDate()));
            table.addCell(String.valueOf(r.getTransactionsCount() == null ? 0 : r.getTransactionsCount()));
            table.addCell(r.getTransactionsVolume() == null ? "0" : r.getTransactionsVolume().toPlainString());
            table.addCell(String.valueOf(r.getActiveUsers() == null ? 0 : r.getActiveUsers()));
        }
        doc.add(table);
        doc.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics-daily.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping(value = "/daily.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportDailyXlsx(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) throws Exception {
        List<DailyAggregate> rows = dailyAggregateRepository.findByDateBetweenOrderByDateAsc(start, end);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Daily");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("date");
            header.createCell(1).setCellValue("transactionsCount");
            header.createCell(2).setCellValue("transactionsVolume");
            header.createCell(3).setCellValue("activeUsers");
            int i = 1;
            for (DailyAggregate r : rows) {
                Row row = sheet.createRow(i++);
                row.createCell(0).setCellValue(String.valueOf(r.getDate()));
                row.createCell(1).setCellValue(r.getTransactionsCount() == null ? 0 : r.getTransactionsCount());
                row.createCell(2).setCellValue(r.getTransactionsVolume() == null ? 0D : r.getTransactionsVolume().doubleValue());
                row.createCell(3).setCellValue(r.getActiveUsers() == null ? 0 : r.getActiveUsers());
            }
            wb.write(baos);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics-daily.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(baos.toByteArray());
        }
    }
}



package com.Project.finance_tracker.controller;

import com.Project.finance_tracker.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/csv")
    public ResponseEntity<ByteArrayResource> exportCsv(
            @RequestParam int month, @RequestParam int year) {

        byte[] data = exportService.generateCsv(month, year);
        String filename = "expense-report-" + year + "-" + month + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }

    @GetMapping("/pdf")
    public ResponseEntity<ByteArrayResource> exportPdf(
            @RequestParam int month, @RequestParam int year) {

        byte[] data = exportService.generatePdf(month, year);
        String filename = "expense-report-" + year + "-" + month + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }
}
package com.klup.protrackr.controller;

import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.ExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/exports")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
public class ExportController {
    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping(value = "/students.csv", produces = "text/csv")
    public ResponseEntity<String> students(@RequestParam(required = false) String department,
                                           @RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) String search) {
        var principal = CurrentUser.require();
        String csv = exportService.exportStudentsCsv(principal, search, department, year);
        return asCsvAttachment("students.csv", csv);
    }

    @GetMapping(value = "/projects.csv", produces = "text/csv")
    public ResponseEntity<String> projects(@RequestParam(required = false) String department,
                                           @RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String semester,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        var principal = CurrentUser.require();
        String csv = exportService.exportProjectsCsv(principal, search, department, year, status, fromDate, toDate);
        return asCsvAttachment("projects.csv", csv);
    }

    @GetMapping(value = "/reviews.csv", produces = "text/csv")
    public ResponseEntity<String> reviews(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        var principal = CurrentUser.require();
        String csv = exportService.exportReviewsCsv(principal, status, fromDate, toDate);
        return asCsvAttachment("reviews.csv", csv);
    }

    @GetMapping(value = "/reports.csv", produces = "text/csv")
    public ResponseEntity<String> reports() {
        var principal = CurrentUser.require();
        String csv = exportService.exportReportsCsv(principal);
        return asCsvAttachment("reports.csv", csv);
    }

    private static ResponseEntity<String> asCsvAttachment(String filename, String body) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}


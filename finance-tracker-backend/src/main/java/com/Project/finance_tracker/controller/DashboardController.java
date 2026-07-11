package com.Project.finance_tracker.controller;

import com.Project.finance_tracker.dto.CategoryBreakdownResponse;
import com.Project.finance_tracker.dto.DashboardSummaryResponse;
import com.Project.finance_tracker.dto.MonthlyTrendResponse;
import com.Project.finance_tracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(dashboardService.getSummary(month, year));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownResponse>> getCategoryBreakdown(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(dashboardService.getCategoryBreakdown(month, year));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int monthsBack) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrend(monthsBack));
    }
}
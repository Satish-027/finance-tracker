package com.Project.finance_tracker.service;

import com.Project.finance_tracker.dto.CategoryBreakdownResponse;
import com.Project.finance_tracker.dto.DashboardSummaryResponse;
import com.Project.finance_tracker.dto.MonthlyTrendResponse;
import com.Project.finance_tracker.entity.TransactionType;
import com.Project.finance_tracker.entity.User;
import com.Project.finance_tracker.repository.TransactionRepository;
import com.Project.finance_tracker.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AuthUtil authUtil;

    public DashboardSummaryResponse getSummary(int month, int year) {
        User user = authUtil.getCurrentUser();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal income = transactionRepository.sumByUserAndTypeAndDateRange(
                user.getId(), TransactionType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumByUserAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE, start, end);

        return DashboardSummaryResponse.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .netSavings(income.subtract(expense))
                .month(month)
                .year(year)
                .build();
    }

    public List<CategoryBreakdownResponse> getCategoryBreakdown(int month, int year) {
        User user = authUtil.getCurrentUser();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Object[]> rows = transactionRepository.sumExpensesByCategory(user.getId(), start, end);

        BigDecimal total = rows.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(r -> {
            String category = (String) r[0];
            BigDecimal amount = (BigDecimal) r[1];
            double percentage = total.compareTo(BigDecimal.ZERO) == 0
                    ? 0.0
                    : amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

            return CategoryBreakdownResponse.builder()
                    .category(category)
                    .totalAmount(amount)
                    .percentage(percentage)
                    .build();
        }).toList();
    }

    public List<MonthlyTrendResponse> getMonthlyTrend(int monthsBack) {
        User user = authUtil.getCurrentUser();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(monthsBack - 1).withDayOfMonth(1);

        List<Object[]> rows = transactionRepository.findMonthlyTrends(user.getId(), start, end);

        Map<String, MonthlyTrendResponse> trendMap = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(start);
        YearMonth endYm = YearMonth.from(end);
        while (!cursor.isAfter(endYm)) {
            String key = cursor.getYear() + "-" + cursor.getMonthValue();
            trendMap.put(key, MonthlyTrendResponse.builder()
                    .month(cursor.getMonthValue())
                    .year(cursor.getYear())
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .build());
            cursor = cursor.plusMonths(1);
        }

        for (Object[] row : rows) {
            int m = ((Number) row[0]).intValue();
            int y = ((Number) row[1]).intValue();
            TransactionType type = (TransactionType) row[2];
            BigDecimal amount = (BigDecimal) row[3];

            String key = y + "-" + m;
            MonthlyTrendResponse existing = trendMap.get(key);
            if (existing == null) continue;

            if (type == TransactionType.INCOME) {
                existing.setIncome(amount);
            } else {
                existing.setExpense(amount);
            }
        }

        return new ArrayList<>(trendMap.values());
    }
}
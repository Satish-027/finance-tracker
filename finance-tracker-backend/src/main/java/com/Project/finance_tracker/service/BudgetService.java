package com.Project.finance_tracker.service;

import com.Project.finance_tracker.dto.BudgetRequest;
import com.Project.finance_tracker.dto.BudgetResponse;
import com.Project.finance_tracker.entity.Budget;
import com.Project.finance_tracker.entity.TransactionType;
import com.Project.finance_tracker.entity.User;
import com.Project.finance_tracker.exception.BadRequestException;
import com.Project.finance_tracker.exception.ResourceNotFoundException;
import com.Project.finance_tracker.repository.BudgetRepository;
import com.Project.finance_tracker.repository.TransactionRepository;
import com.Project.finance_tracker.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final AuthUtil authUtil;

    public BudgetResponse create(BudgetRequest request) {
        User user = authUtil.getCurrentUser();

        budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                user.getId(), request.getCategory(), request.getMonth(), request.getYear()
        ).ifPresent(b -> {
            throw new BadRequestException("Budget already exists for this category and month");
        });

        Budget budget = Budget.builder()
                .user(user)
                .category(request.getCategory())
                .limitAmount(request.getLimitAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .build();

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    public List<BudgetResponse> getByMonth(int month, int year) {
        User user = authUtil.getCurrentUser();
        return budgetRepository.findByUserIdAndMonthAndYear(user.getId(), month, year)
                .stream().map(this::toResponse).toList();
    }

    public BudgetResponse update(Long id, BudgetRequest request) {
        User user = authUtil.getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Budget not found");
        }

        budget.setCategory(request.getCategory());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());

        Budget updated = budgetRepository.save(budget);
        return toResponse(updated);
    }

    public void delete(Long id) {
        User user = authUtil.getCurrentUser();

        if (!budgetRepository.existsByIdAndUserId(id, user.getId())) {
            throw new ResourceNotFoundException("Budget not found");
        }

        budgetRepository.deleteById(id);
    }

    private BudgetResponse toResponse(Budget budget) {
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal spent = transactionRepository.sumByUserAndTypeAndDateRange(
                budget.getUser().getId(), TransactionType.EXPENSE, start, end);

        // Filter spent to this category only — sumByUserAndTypeAndDateRange sums all categories,
        // so we recalculate here scoped to category using existing transactions.
        BigDecimal categorySpent = transactionRepository
                .findByUserIdAndCategoryOrderByTransactionDateDesc(budget.getUser().getId(), budget.getCategory())
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getTransactionDate().isBefore(start) && !t.getTransactionDate().isAfter(end))
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = budget.getLimitAmount().subtract(categorySpent);
        boolean overBudget = categorySpent.compareTo(budget.getLimitAmount()) > 0;
        double percentUsed = categorySpent
                .divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .limitAmount(budget.getLimitAmount())
                .spentAmount(categorySpent)
                .remainingAmount(remaining)
                .overBudget(overBudget)
                .percentUsed(percentUsed)
                .month(budget.getMonth())
                .year(budget.getYear())
                .build();
    }
}
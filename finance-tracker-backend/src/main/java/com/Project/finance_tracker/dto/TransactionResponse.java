package com.Project.finance_tracker.dto;

import com.Project.finance_tracker.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private String category;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
}
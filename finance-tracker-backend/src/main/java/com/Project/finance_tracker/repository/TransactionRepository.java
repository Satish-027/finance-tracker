package com.Project.finance_tracker.repository;

import com.Project.finance_tracker.entity.Transaction;
import com.Project.finance_tracker.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId, LocalDate start, LocalDate end);

    List<Transaction> findByUserIdAndCategoryOrderByTransactionDateDesc(Long userId, String category);

    List<Transaction> findByUserIdAndTypeOrderByTransactionDateDesc(Long userId, TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = :type " +
            "AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumByUserAndTypeAndDateRange(@Param("userId") Long userId,
                                            @Param("type") TransactionType type,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
            "AND t.transactionDate BETWEEN :start AND :end GROUP BY t.category")
    List<Object[]> sumExpensesByCategory(@Param("userId") Long userId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    @Query("SELECT EXTRACT(MONTH FROM t.transactionDate), EXTRACT(YEAR FROM t.transactionDate), " +
            "t.type, COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY EXTRACT(MONTH FROM t.transactionDate), EXTRACT(YEAR FROM t.transactionDate), t.type " +
            "ORDER BY EXTRACT(YEAR FROM t.transactionDate), EXTRACT(MONTH FROM t.transactionDate)")
    List<Object[]> findMonthlyTrends(@Param("userId") Long userId,
                                     @Param("start") LocalDate start,
                                     @Param("end") LocalDate end);

    boolean existsByIdAndUserId(Long id, Long userId);
}
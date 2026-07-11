package com.Project.finance_tracker.service;

import com.Project.finance_tracker.dto.TransactionRequest;
import com.Project.finance_tracker.dto.TransactionResponse;
import com.Project.finance_tracker.entity.Transaction;
import com.Project.finance_tracker.entity.User;
import com.Project.finance_tracker.exception.ResourceNotFoundException;
import com.Project.finance_tracker.repository.TransactionRepository;
import com.Project.finance_tracker.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuthUtil authUtil;

    public TransactionResponse create(TransactionRequest request) {
        User user = authUtil.getCurrentUser();

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(request.getType())
                .category(request.getCategory())
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public List<TransactionResponse> getAll() {
        User user = authUtil.getCurrentUser();
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public TransactionResponse update(Long id, TransactionRequest request) {
        User user = authUtil.getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    public void delete(Long id) {
        User user = authUtil.getCurrentUser();

        if (!transactionRepository.existsByIdAndUserId(id, user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        transactionRepository.deleteById(id);
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .type(t.getType())
                .category(t.getCategory())
                .amount(t.getAmount())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .build();
    }
}
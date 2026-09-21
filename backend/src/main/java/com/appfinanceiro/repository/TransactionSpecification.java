package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Transaction;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> belongsToUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> dateAfterOrEqual(LocalDate startDate) {
        if (startDate == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate);
    }

    public static Specification<Transaction> dateBeforeOrEqual(LocalDate endDate) {
        if (endDate == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate);
    }

    public static Specification<Transaction> hasAccount(UUID accountId) {
        if (accountId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transaction> hasCreditCard(UUID creditCardId) {
        if (creditCardId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("creditCard").get("id"), creditCardId);
    }

    public static Specification<Transaction> hasCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> descriptionContains(String search) {
        if (search == null || search.isBlank()) return null;
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("description")),
                "%" + search.toLowerCase() + "%"
        );
    }
}

package com.example.orders.grpc;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.ValidationResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class OrderService {
    private final Map<String, OrderRecord> orders = new ConcurrentHashMap<>();

    OrderResult create(String orderId, String customerId, String product) {
        ValidationResult validation = ValidationResult.combine(
            required("orderId", orderId),
            required("customerId", customerId),
            required("product", product)
        );
        if (validation.isInvalid()) {
            return OrderResult.failure(validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT));
        }
        if ("blocked".equalsIgnoreCase(product)) {
            return OrderResult.failure(Outcome.of(OrderOutcomes.ORDER_REJECTED));
        }
        OrderRecord order = new OrderRecord(orderId, customerId, product, "CREATED");
        if (orders.putIfAbsent(orderId, order) != null) {
            return OrderResult.failure(Outcome.of(StandardOutcomes.ALREADY_EXISTS));
        }
        return new OrderResult(order, Outcome.of(OrderOutcomes.ORDER_CREATED));
    }

    OrderResult find(String orderId) {
        OrderRecord order = orders.get(orderId);
        return order == null
            ? OrderResult.failure(Outcome.of(StandardOutcomes.NOT_FOUND))
            : new OrderResult(order, Outcome.of(StandardOutcomes.OK));
    }

    private static ValidationResult required(String path, String value) {
        return value == null || value.isBlank()
            ? ValidationResult.invalid(Issue.at(path, "The value is required."))
            : ValidationResult.valid();
    }
}

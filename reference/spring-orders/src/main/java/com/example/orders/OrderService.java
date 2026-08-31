package com.example.orders;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.ValidationResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
class OrderService {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    OrderResult<Order> create(CreateOrderRequest request) {
        ValidationResult validation = ValidationResult.combine(
            required("orderId", request.orderId()),
            required("customerId", request.customerId()),
            required("product", request.product())
        );
        if (validation.isInvalid()) {
            return OrderResult.failure(validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT));
        }

        Order order = new Order(request.orderId(), request.customerId(), request.product(), "CREATED");
        if (orders.putIfAbsent(order.orderId(), order) != null) {
            return OrderResult.failure(Outcome.of(StandardOutcomes.ALREADY_EXISTS));
        }
        return OrderResult.of(order, Outcome.of(OrderOutcomes.ORDER_CREATED));
    }

    OrderResult<Order> find(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            return OrderResult.failure(Outcome.of(StandardOutcomes.NOT_FOUND));
        }
        return OrderResult.of(order, Outcome.of(StandardOutcomes.OK));
    }

    OrderResult<Order> process(String orderId) {
        Order current = orders.get(orderId);
        if (current == null) {
            return OrderResult.failure(Outcome.of(StandardOutcomes.NOT_FOUND));
        }
        if ("blocked".equalsIgnoreCase(current.product())) {
            return OrderResult.failure(Outcome.of(OrderOutcomes.ORDER_REJECTED));
        }
        Order processing = current.withStatus("PROCESSING");
        orders.put(orderId, processing);
        return OrderResult.of(processing, Outcome.of(OrderOutcomes.ORDER_PROCESSING));
    }

    private static ValidationResult required(String path, String value) {
        return value == null || value.isBlank()
            ? ValidationResult.invalid(Issue.at(path, "The value is required."))
            : ValidationResult.valid();
    }
}

package com.example.orders;

import io.github.aalsanie.codes.ProblemType;
import java.net.URI;

final class OrderProblems {
    static final ProblemType ORDER_NOT_FOUND = ProblemType.of(
        URI.create("https://api.example.com/problems/order-not-found"),
        404,
        "Order not found"
    );

    static final ProblemType ORDER_ALREADY_CANCELLED = ProblemType.of(
        URI.create("https://api.example.com/problems/order-already-cancelled"),
        409,
        "Order already cancelled"
    );

    static final ProblemType INVALID_ORDER_STATE = ProblemType.of(
        URI.create("https://api.example.com/problems/invalid-order-state"),
        409,
        "Invalid order state"
    );

    private OrderProblems() {
    }
}

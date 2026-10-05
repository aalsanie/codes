package com.example.orders;

import io.github.aalsanie.codes.ProblemType;
import java.net.URI;

enum OrderProblems implements ProblemType {
    ORDER_NOT_FOUND(
        "https://api.example.com/problems/order-not-found",
        404,
        "Order not found"
    ),
    ORDER_ALREADY_CANCELLED(
        "https://api.example.com/problems/order-already-cancelled",
        409,
        "Order already cancelled"
    ),
    INVALID_ORDER_STATE(
        "https://api.example.com/problems/invalid-order-state",
        409,
        "Invalid order state"
    );

    private final URI type;
    private final int status;
    private final String title;

    OrderProblems(String type, int status, String title) {
        this.type = URI.create(type);
        this.status = status;
        this.title = title;
    }

    @Override
    public URI getType() {
        return type;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getTitle() {
        return title;
    }
}

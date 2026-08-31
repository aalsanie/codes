package com.example.orders.grpc;

import io.github.aalsanie.codes.Outcome;
import java.util.Objects;

record OrderResult(OrderRecord value, Outcome outcome) {
    OrderResult {
        Objects.requireNonNull(outcome, "outcome");
    }

    static OrderResult failure(Outcome outcome) {
        return new OrderResult(null, outcome);
    }
}

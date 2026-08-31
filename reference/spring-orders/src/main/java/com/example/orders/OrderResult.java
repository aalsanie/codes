package com.example.orders;

import io.github.aalsanie.codes.Outcome;
import java.util.Objects;

record OrderResult<T>(T value, Outcome outcome) {
    OrderResult {
        Objects.requireNonNull(outcome, "outcome");
    }

    static <T> OrderResult<T> of(T value, Outcome outcome) {
        return new OrderResult<>(value, outcome);
    }

    static <T> OrderResult<T> failure(Outcome outcome) {
        return new OrderResult<>(null, outcome);
    }
}

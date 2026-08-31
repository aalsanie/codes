package com.example.orders;

import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;

final class OrderOutcomes {
    static final OutcomeDefinition ORDER_CREATED = OutcomeDefinition.custom(
        "com.example.orders", "ORDER_CREATED", OutcomeState.SUCCEEDED, "The order was created."
    );
    static final OutcomeDefinition ORDER_PROCESSING = OutcomeDefinition.custom(
        "com.example.orders", "ORDER_PROCESSING", OutcomeState.PENDING, "The order is processing."
    );
    static final OutcomeDefinition ORDER_REJECTED = OutcomeDefinition.custom(
        "com.example.orders", "ORDER_REJECTED", OutcomeState.FAILED, "The order was rejected."
    );

    private OrderOutcomes() {
    }
}

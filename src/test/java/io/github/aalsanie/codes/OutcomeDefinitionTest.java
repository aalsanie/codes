package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OutcomeDefinitionTest {
    @Test
    void createsCustomDefinition() {
        OutcomeDefinition definition = OutcomeDefinition.custom(
            "com.example.orders",
            "ORDER_REJECTED",
            OutcomeState.FAILED,
            "Order rejected."
        );
        assertEquals("com.example.orders:ORDER_REJECTED", definition.getCode().getValue());
        assertEquals(OutcomeState.FAILED, definition.getState());
        assertEquals("Order rejected.", definition.getDefaultMessage());
        assertTrue(definition.toString().contains("ORDER_REJECTED"));
    }

    @Test
    void customDefinitionRejectsReservedNamespaceAndBadMessages() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OutcomeDefinition.custom(StandardOutcomes.NAMESPACE, "CUSTOM", OutcomeState.FAILED, "No.")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> OutcomeDefinition.custom("io.github.aalsanie.codes.child", "CUSTOM", OutcomeState.FAILED, "No.")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, " ")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, "x".repeat(1025))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, "bad\0message")
        );
        assertThrows(NullPointerException.class, () -> OutcomeDefinition.custom((OutcomeCode) null, OutcomeState.FAILED, "No."));
        assertThrows(NullPointerException.class, () -> OutcomeDefinition.custom(OutcomeCode.of("com.example", "X"), null, "No."));
    }

    @Test
    void standardDefinitionIsSingleton() {
        assertSame(StandardOutcomes.NOT_FOUND, OutcomeDefinition.standard("NOT_FOUND"));
        assertThrows(IllegalStateException.class, () -> OutcomeDefinition.standard("NOPE"));
    }
}

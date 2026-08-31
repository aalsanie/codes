package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class RegistryMappingExceptionTest {
    @Test
    void registryIsImmutableAndKeyedByCode() {
        OutcomeDefinition custom = OutcomeDefinition.custom(
            "com.example.orders", "ORDER_REJECTED", OutcomeState.FAILED, "Rejected."
        );
        OutcomeRegistry empty = OutcomeRegistry.empty();
        OutcomeRegistry registry = empty.with(custom);

        assertEquals(0, empty.getSize());
        assertEquals(1, registry.getSize());
        assertTrue(registry.contains(custom.getCode()));
        assertSame(custom, registry.find(custom.getCode()));
        assertSame(custom, registry.find(custom.getCode().getValue()));
        assertNull(registry.find("invalid"));
        assertSame(custom, registry.require(custom.getCode()));
        assertEquals(List.of(custom), registry.definitions());
        assertThrows(UnsupportedOperationException.class, () -> registry.definitions().add(StandardOutcomes.OK));
        assertThrows(IllegalArgumentException.class, () -> registry.with(custom));
        assertThrows(NoSuchElementException.class, () -> registry.require(StandardOutcomes.NOT_FOUND.getCode()));
    }

    @Test
    void registryFactoriesComposeDefinitions() {
        OutcomeDefinition first = OutcomeDefinition.custom("com.example", "A", OutcomeState.FAILED, "A.");
        OutcomeDefinition second = OutcomeDefinition.custom("com.example", "B", OutcomeState.FAILED, "B.");
        assertEquals(2, OutcomeRegistry.of(first, second).getSize());
        assertEquals(StandardOutcomes.all.size(), OutcomeRegistry.standard().getSize());
        assertThrows(NullPointerException.class, () -> OutcomeRegistry.of((OutcomeDefinition[]) null));
        assertThrows(NullPointerException.class, () -> OutcomeRegistry.empty().with(null));
        assertThrows(NullPointerException.class, () -> OutcomeRegistry.empty().withAll(null));
    }

    @Test
    void exceptionRequiresFailedOutcomeAndPreservesCause() {
        Outcome failure = Outcome.of(StandardOutcomes.INTERNAL);
        RuntimeException cause = new RuntimeException("cause");
        OutcomeException exception = new OutcomeException(failure, cause);
        assertSame(failure, exception.getOutcome());
        assertSame(cause, exception.getCause());
        assertEquals(failure.getMessage(), exception.getMessage());
        assertSame(failure, OutcomeExceptions.toException(failure).getOutcome());
        assertSame(cause, OutcomeExceptions.toException(failure, cause).getCause());
        assertThrows(IllegalArgumentException.class, () -> new OutcomeException(Outcome.of(StandardOutcomes.OK)));
        assertThrows(NullPointerException.class, () -> new OutcomeException(null));
    }
}

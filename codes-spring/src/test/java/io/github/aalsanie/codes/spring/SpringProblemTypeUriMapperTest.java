package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import java.net.URI;
import org.junit.jupiter.api.Test;

class SpringProblemTypeUriMapperTest {
    private static final OutcomeDefinition PAYMENT_DECLINED = OutcomeDefinition.custom(
        "com.example.checkout",
        "PAYMENT_DECLINED",
        OutcomeState.FAILED,
        "The payment was declined."
    );

    @Test
    void emptyMapperIsExplicitlyUnmapped() {
        assertTrue(SpringProblemTypeUriMapper.empty().map(PAYMENT_DECLINED).isUnmapped());
    }

    @Test
    void mapsDefinitionsAndOccurrencesByStableOutcomeCode() {
        URI type = URI.create("https://api.example.test/problems/payment-declined");
        SpringProblemTypeUriMapper mapper = SpringProblemTypeUriMapper.empty()
            .withMapping(PAYMENT_DECLINED, type);
        OutcomeDefinition sameIdentity = OutcomeDefinition.custom(
            "com.example.checkout",
            "PAYMENT_DECLINED",
            OutcomeState.FAILED,
            "A different human-readable message."
        );

        assertEquals(type, mapper.map(sameIdentity).orNull());
        assertEquals(type, mapper.map(Outcome.of(sameIdentity)).orNull());
    }

    @Test
    void duplicateApplicationMappingIsRejected() {
        URI type = URI.create("https://api.example.test/problems/payment-declined");
        SpringProblemTypeUriMapper mapper = SpringProblemTypeUriMapper.empty()
            .withMapping(PAYMENT_DECLINED, type);

        assertThrows(
            IllegalArgumentException.class,
            () -> mapper.withMapping(PAYMENT_DECLINED, type)
        );
    }
}

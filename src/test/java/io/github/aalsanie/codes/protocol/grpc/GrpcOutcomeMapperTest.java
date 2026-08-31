package io.github.aalsanie.codes.protocol.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import org.junit.jupiter.api.Test;

class GrpcOutcomeMapperTest {
    @Test
    void standardMapperCoversAllStandardOutcomes() {
        GrpcOutcomeMapper mapper = GrpcOutcomeMapper.standard();
        assertEquals(StandardOutcomes.all.size(), mapper.getSize());
        assertTrue(StandardOutcomes.all.stream().allMatch(definition -> mapper.map(definition).isMapped()));
        assertSame(GrpcStatusCode.NOT_FOUND, mapper.map(StandardOutcomes.NOT_FOUND).orNull());
        assertSame(GrpcStatusCode.RESOURCE_EXHAUSTED, mapper.map(StandardOutcomes.RATE_LIMITED).orNull());
        assertSame(GrpcStatusCode.RESOURCE_EXHAUSTED, mapper.map(StandardOutcomes.RESOURCE_EXHAUSTED).orNull());
    }

    @Test
    void customMappingsAreImmutableAndExplicit() {
        OutcomeDefinition custom = OutcomeDefinition.custom(
            "com.example", "ORDER_REJECTED", OutcomeState.FAILED, "Rejected."
        );
        GrpcOutcomeMapper base = GrpcOutcomeMapper.standard();
        GrpcOutcomeMapper extended = base.withMapping(custom, GrpcStatusCode.FAILED_PRECONDITION);
        assertFalse(base.contains(custom.getCode()));
        assertTrue(extended.contains(custom.getCode()));
        assertSame(GrpcStatusCode.FAILED_PRECONDITION, extended.map(custom).orNull());
        assertSame(GrpcStatusCode.UNKNOWN, base.withOverride(StandardOutcomes.INTERNAL, GrpcStatusCode.UNKNOWN).map(StandardOutcomes.INTERNAL).orNull());
        assertThrows(IllegalArgumentException.class, () -> base.withMapping(StandardOutcomes.OK, GrpcStatusCode.OK));
        assertThrows(IllegalArgumentException.class, () -> base.withOverride(custom, GrpcStatusCode.UNKNOWN));
        assertThrows(UnsupportedOperationException.class, () -> base.mappings().clear());
    }

    @Test
    void statusCodesRoundTripByNumericValue() {
        for (GrpcStatusCode code : GrpcStatusCode.values()) {
            assertSame(code, GrpcStatusCode.fromValue(code.getValue()));
        }
        assertNull(GrpcStatusCode.fromValue(-1));
        assertNull(GrpcStatusCode.fromValue(17));
    }
}

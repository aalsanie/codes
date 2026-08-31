package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import org.junit.jupiter.api.Test;

class SpringHttpStatusMapperTest {
    @Test
    void mapsCoreHttpStatusToSpringStatus() {
        SpringHttpStatusMapper mapper = SpringHttpStatusMapper.standard();
        assertEquals(404, mapper.map(StandardOutcomes.NOT_FOUND).orNull().value());
        assertTrue(mapper.map(StandardOutcomes.FAILED_PRECONDITION).isUnmapped());
    }

    @Test
    void acceptsApplicationHttpPolicy() {
        OutcomeDefinition rejected = OutcomeDefinition.custom(
            "com.example.orders", "ORDER_REJECTED", OutcomeState.FAILED, "Rejected."
        );
        HttpOutcomeMapper core = HttpOutcomeMapper.standard().withMapping(rejected, HttpStatusCode.of(422));
        assertEquals(422, new SpringHttpStatusMapper(core).map(rejected).orNull().value());
    }
}

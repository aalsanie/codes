package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import org.junit.jupiter.api.Test;
import org.springframework.web.ErrorResponseException;

class SpringOutcomeExceptionsTest {
    @Test
    void convertsMappedFailureWithoutChangingProblemBody() {
        Outcome outcome = Outcome.of(StandardOutcomes.NOT_FOUND);
        MappingResult<ErrorResponseException> result =
            SpringOutcomeExceptions.toErrorResponseException(
                outcome,
                OutcomeProblemDetailMapper.safeDefaults()
            );

        ErrorResponseException exception = result.orNull();

        assertEquals(404, exception.getStatusCode().value());
        assertEquals(404, exception.getBody().getStatus());
        assertEquals(
            outcome.getCode().getValue(),
            exception.getBody().getProperties().get(OutcomeProblemDetailMapper.CODE_PROPERTY)
        );
    }

    @Test
    void preservesUnmappedOutcome() {
        assertTrue(
            SpringOutcomeExceptions.toErrorResponseException(
                Outcome.of(StandardOutcomes.FAILED_PRECONDITION),
                OutcomeProblemDetailMapper.safeDefaults()
            ).isUnmapped()
        );
    }

    @Test
    void rejectsSuccessfulOutcome() {
        assertThrows(
            IllegalArgumentException.class,
            () -> SpringOutcomeExceptions.toErrorResponseException(
                Outcome.of(StandardOutcomes.OK),
                OutcomeProblemDetailMapper.safeDefaults()
            )
        );
    }
}

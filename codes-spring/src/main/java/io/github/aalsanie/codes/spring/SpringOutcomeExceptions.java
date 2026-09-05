package io.github.aalsanie.codes.spring;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

/**
 * Thin Spring exception bridge for failed Codes outcomes.
 */
public final class SpringOutcomeExceptions {
    private SpringOutcomeExceptions() {
    }

    public static MappingResult<ErrorResponseException> toErrorResponseException(
        Outcome outcome,
        OutcomeProblemDetailMapper mapper
    ) {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(mapper, "mapper");

        return mapper.map(outcome).fold(
            problem -> MappingResult.mapped(
                new ErrorResponseException(
                    HttpStatusCode.valueOf(problem.getStatus()),
                    problem,
                    null
                )
            ),
            MappingResult::unmapped
        );
    }
}

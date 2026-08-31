package io.github.aalsanie.codes.spring;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;

public final class SpringHttpStatusMapper {
    private final HttpOutcomeMapper mapper;

    public SpringHttpStatusMapper(HttpOutcomeMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public static SpringHttpStatusMapper standard() {
        return new SpringHttpStatusMapper(HttpOutcomeMapper.standard());
    }

    public MappingResult<HttpStatusCode> map(OutcomeDefinition definition) {
        return mapper.map(Objects.requireNonNull(definition, "definition"))
            .fold(
                status -> MappingResult.mapped(HttpStatusCode.valueOf(status.getValue())),
                MappingResult::unmapped
            );
    }

    public MappingResult<HttpStatusCode> map(Outcome outcome) {
        return map(Objects.requireNonNull(outcome, "outcome").getDefinition());
    }
}

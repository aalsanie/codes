package io.github.aalsanie.codes;

import java.util.Objects;

public interface OutcomeMapper<T> {
    MappingResult<T> map(OutcomeDefinition definition);

    default MappingResult<T> map(Outcome outcome) {
        return map(Objects.requireNonNull(outcome, "outcome").getDefinition());
    }
}

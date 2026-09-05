package io.github.aalsanie.codes.spring;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeCode;
import io.github.aalsanie.codes.OutcomeDefinition;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable application-owned mapping from Codes outcome identity to RFC 9457 problem type URI.
 *
 * <p>Codes does not assign or invent problem type URIs. Applications opt in by registering the
 * URIs whose ownership and lifecycle they control.
 */
public final class SpringProblemTypeUriMapper {
    private final Map<OutcomeCode, URI> mappings;

    private SpringProblemTypeUriMapper(Map<OutcomeCode, URI> mappings) {
        this.mappings = Collections.unmodifiableMap(new LinkedHashMap<>(mappings));
    }

    public static SpringProblemTypeUriMapper empty() {
        return new SpringProblemTypeUriMapper(Map.of());
    }

    public MappingResult<URI> map(OutcomeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        URI type = mappings.get(definition.getCode());
        return type == null ? MappingResult.unmapped() : MappingResult.mapped(type);
    }

    public MappingResult<URI> map(Outcome outcome) {
        return map(Objects.requireNonNull(outcome, "outcome").getDefinition());
    }

    public SpringProblemTypeUriMapper withMapping(OutcomeDefinition definition, URI type) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(type, "type");
        OutcomeCode code = definition.getCode();
        if (mappings.containsKey(code)) {
            throw new IllegalArgumentException("problem type mapping already exists for outcome code: " + code);
        }

        LinkedHashMap<OutcomeCode, URI> next = new LinkedHashMap<>(mappings);
        next.put(code, type);
        return new SpringProblemTypeUriMapper(next);
    }
}

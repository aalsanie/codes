package io.github.aalsanie.codes.protocol.http;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.OutcomeCode;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeMapper;
import io.github.aalsanie.codes.StandardOutcomes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class HttpOutcomeMapper implements OutcomeMapper<HttpStatusCode> {
    private final Map<OutcomeCode, HttpStatusCode> mappings;

    private HttpOutcomeMapper(Map<OutcomeCode, HttpStatusCode> mappings) {
        this.mappings = Collections.unmodifiableMap(new LinkedHashMap<>(mappings));
    }

    public static HttpOutcomeMapper empty() {
        return new HttpOutcomeMapper(Map.of());
    }

    public static HttpOutcomeMapper standard() {
        LinkedHashMap<OutcomeCode, HttpStatusCode> values = new LinkedHashMap<>();
        values.put(StandardOutcomes.OK.getCode(), HttpStatusCode.OK);
        values.put(StandardOutcomes.INVALID_ARGUMENT.getCode(), HttpStatusCode.BAD_REQUEST);
        values.put(StandardOutcomes.UNAUTHENTICATED.getCode(), HttpStatusCode.UNAUTHORIZED);
        values.put(StandardOutcomes.PERMISSION_DENIED.getCode(), HttpStatusCode.FORBIDDEN);
        values.put(StandardOutcomes.NOT_FOUND.getCode(), HttpStatusCode.NOT_FOUND);
        values.put(StandardOutcomes.ALREADY_EXISTS.getCode(), HttpStatusCode.CONFLICT);
        values.put(StandardOutcomes.OUT_OF_RANGE.getCode(), HttpStatusCode.BAD_REQUEST);
        values.put(StandardOutcomes.RATE_LIMITED.getCode(), HttpStatusCode.TOO_MANY_REQUESTS);
        values.put(StandardOutcomes.UNIMPLEMENTED.getCode(), HttpStatusCode.NOT_IMPLEMENTED);
        values.put(StandardOutcomes.UNAVAILABLE.getCode(), HttpStatusCode.SERVICE_UNAVAILABLE);
        values.put(StandardOutcomes.INTERNAL.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR);
        values.put(StandardOutcomes.DATA_LOSS.getCode(), HttpStatusCode.INTERNAL_SERVER_ERROR);
        return new HttpOutcomeMapper(values);
    }

    @Override
    public MappingResult<HttpStatusCode> map(OutcomeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        HttpStatusCode status = mappings.get(definition.getCode());
        return status == null ? MappingResult.unmapped() : MappingResult.mapped(status);
    }

    public int getSize() {
        return mappings.size();
    }

    public boolean contains(OutcomeCode code) {
        return mappings.containsKey(Objects.requireNonNull(code, "code"));
    }

    public HttpOutcomeMapper withMapping(OutcomeDefinition definition, HttpStatusCode status) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(status, "status");
        OutcomeCode code = definition.getCode();
        if (mappings.containsKey(code)) {
            throw new IllegalArgumentException("mapping already exists for outcome code: " + code);
        }
        LinkedHashMap<OutcomeCode, HttpStatusCode> next = new LinkedHashMap<>(mappings);
        next.put(code, status);
        return new HttpOutcomeMapper(next);
    }

    public HttpOutcomeMapper withOverride(OutcomeDefinition definition, HttpStatusCode status) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(status, "status");
        OutcomeCode code = definition.getCode();
        if (!mappings.containsKey(code)) {
            throw new IllegalArgumentException("cannot override missing mapping for outcome code: " + code);
        }
        LinkedHashMap<OutcomeCode, HttpStatusCode> next = new LinkedHashMap<>(mappings);
        next.put(code, status);
        return new HttpOutcomeMapper(next);
    }

    public Map<OutcomeCode, HttpStatusCode> mappings() {
        return mappings;
    }
}

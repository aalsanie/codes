package io.github.aalsanie.codes.protocol.grpc;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.OutcomeCode;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeMapper;
import io.github.aalsanie.codes.StandardOutcomes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GrpcOutcomeMapper implements OutcomeMapper<GrpcStatusCode> {
    private final Map<OutcomeCode, GrpcStatusCode> mappings;

    private GrpcOutcomeMapper(Map<OutcomeCode, GrpcStatusCode> mappings) {
        this.mappings = Collections.unmodifiableMap(new LinkedHashMap<>(mappings));
    }

    public static GrpcOutcomeMapper empty() {
        return new GrpcOutcomeMapper(Map.of());
    }

    public static GrpcOutcomeMapper standard() {
        LinkedHashMap<OutcomeCode, GrpcStatusCode> values = new LinkedHashMap<>();
        values.put(StandardOutcomes.OK.getCode(), GrpcStatusCode.OK);
        values.put(StandardOutcomes.INVALID_ARGUMENT.getCode(), GrpcStatusCode.INVALID_ARGUMENT);
        values.put(StandardOutcomes.UNAUTHENTICATED.getCode(), GrpcStatusCode.UNAUTHENTICATED);
        values.put(StandardOutcomes.PERMISSION_DENIED.getCode(), GrpcStatusCode.PERMISSION_DENIED);
        values.put(StandardOutcomes.NOT_FOUND.getCode(), GrpcStatusCode.NOT_FOUND);
        values.put(StandardOutcomes.ALREADY_EXISTS.getCode(), GrpcStatusCode.ALREADY_EXISTS);
        values.put(StandardOutcomes.FAILED_PRECONDITION.getCode(), GrpcStatusCode.FAILED_PRECONDITION);
        values.put(StandardOutcomes.OUT_OF_RANGE.getCode(), GrpcStatusCode.OUT_OF_RANGE);
        values.put(StandardOutcomes.RATE_LIMITED.getCode(), GrpcStatusCode.RESOURCE_EXHAUSTED);
        values.put(StandardOutcomes.CANCELLED.getCode(), GrpcStatusCode.CANCELLED);
        values.put(StandardOutcomes.DEADLINE_EXCEEDED.getCode(), GrpcStatusCode.DEADLINE_EXCEEDED);
        values.put(StandardOutcomes.ABORTED.getCode(), GrpcStatusCode.ABORTED);
        values.put(StandardOutcomes.UNIMPLEMENTED.getCode(), GrpcStatusCode.UNIMPLEMENTED);
        values.put(StandardOutcomes.UNAVAILABLE.getCode(), GrpcStatusCode.UNAVAILABLE);
        values.put(StandardOutcomes.INTERNAL.getCode(), GrpcStatusCode.INTERNAL);
        values.put(StandardOutcomes.DATA_LOSS.getCode(), GrpcStatusCode.DATA_LOSS);
        values.put(StandardOutcomes.RESOURCE_EXHAUSTED.getCode(), GrpcStatusCode.RESOURCE_EXHAUSTED);
        return new GrpcOutcomeMapper(values);
    }

    @Override
    public MappingResult<GrpcStatusCode> map(OutcomeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        GrpcStatusCode status = mappings.get(definition.getCode());
        return status == null ? MappingResult.unmapped() : MappingResult.mapped(status);
    }

    public int getSize() {
        return mappings.size();
    }

    public boolean contains(OutcomeCode code) {
        return mappings.containsKey(Objects.requireNonNull(code, "code"));
    }

    public GrpcOutcomeMapper withMapping(OutcomeDefinition definition, GrpcStatusCode status) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(status, "status");
        OutcomeCode code = definition.getCode();
        if (mappings.containsKey(code)) {
            throw new IllegalArgumentException("mapping already exists for outcome code: " + code);
        }
        LinkedHashMap<OutcomeCode, GrpcStatusCode> next = new LinkedHashMap<>(mappings);
        next.put(code, status);
        return new GrpcOutcomeMapper(next);
    }

    public GrpcOutcomeMapper withOverride(OutcomeDefinition definition, GrpcStatusCode status) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(status, "status");
        OutcomeCode code = definition.getCode();
        if (!mappings.containsKey(code)) {
            throw new IllegalArgumentException("cannot override missing mapping for outcome code: " + code);
        }
        LinkedHashMap<OutcomeCode, GrpcStatusCode> next = new LinkedHashMap<>(mappings);
        next.put(code, status);
        return new GrpcOutcomeMapper(next);
    }

    public Map<OutcomeCode, GrpcStatusCode> mappings() {
        return mappings;
    }
}

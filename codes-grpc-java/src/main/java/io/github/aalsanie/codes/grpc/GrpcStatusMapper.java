package io.github.aalsanie.codes.grpc;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.grpc.Status;
import java.util.Objects;

public final class GrpcStatusMapper {
    private final GrpcOutcomeMapper mapper;

    public GrpcStatusMapper(GrpcOutcomeMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public static GrpcStatusMapper standard() {
        return new GrpcStatusMapper(GrpcOutcomeMapper.standard());
    }

    public MappingResult<Status> map(OutcomeDefinition definition) {
        return mapper.map(Objects.requireNonNull(definition, "definition"))
            .fold(
                status -> MappingResult.mapped(Status.fromCodeValue(status.getValue())),
                MappingResult::unmapped
            );
    }

    public MappingResult<Status> map(Outcome outcome) {
        return map(Objects.requireNonNull(outcome, "outcome").getDefinition());
    }
}

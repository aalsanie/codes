package io.github.aalsanie.codes.grpc;

import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import java.util.Objects;

public final class GrpcOutcomeExceptions {
    private GrpcOutcomeExceptions() {
    }

    public static MappingResult<StatusRuntimeException> toStatusRuntimeException(
        Outcome outcome,
        GoogleRpcOutcomeMapper mapper
    ) {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(mapper, "mapper");
        return mapper.map(outcome).fold(
            status -> MappingResult.mapped(StatusProto.toStatusRuntimeException(status)),
            MappingResult::unmapped
        );
    }
}

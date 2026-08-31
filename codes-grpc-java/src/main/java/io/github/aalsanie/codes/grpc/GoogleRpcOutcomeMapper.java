package io.github.aalsanie.codes.grpc;

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import com.google.rpc.DebugInfo;
import com.google.rpc.ErrorInfo;
import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import java.util.Objects;

public final class GoogleRpcOutcomeMapper {
    private final GrpcOutcomeMapper mapper;
    private final GrpcOutcomeExposure exposure;

    public GoogleRpcOutcomeMapper(GrpcOutcomeMapper mapper, GrpcOutcomeExposure exposure) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.exposure = Objects.requireNonNull(exposure, "exposure");
    }

    public static GoogleRpcOutcomeMapper safeDefaults() {
        return new GoogleRpcOutcomeMapper(
            GrpcOutcomeMapper.standard(),
            GrpcOutcomeExposure.safeDefaults()
        );
    }

    public MappingResult<com.google.rpc.Status> map(Outcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        if (!outcome.isFailed()) {
            throw new IllegalArgumentException("google.rpc.Status error details are only produced for failed outcomes");
        }
        return mapper.map(outcome).fold(
            status -> MappingResult.mapped(createStatus(outcome, status.getValue())),
            MappingResult::unmapped
        );
    }

    private com.google.rpc.Status createStatus(Outcome outcome, int statusCode) {
        com.google.rpc.Status.Builder status = com.google.rpc.Status.newBuilder()
            .setCode(statusCode)
            .setMessage(exposure.exposeMessage() ? outcome.getMessage() : outcome.getCode().getValue())
            .addDetails(Any.pack(
                ErrorInfo.newBuilder()
                    .setDomain(outcome.getCode().getNamespace())
                    .setReason(outcome.getCode().getName())
                    .build()
            ));

        if (exposure.exposeDetail() && outcome.getDetail() != null) {
            status.addDetails(Any.pack(
                DebugInfo.newBuilder()
                    .setDetail(outcome.getDetail())
                    .build()
            ));
        }

        if (exposure.exposeIssues() && !outcome.getIssues().isEmpty()) {
            BadRequest.Builder request = BadRequest.newBuilder();
            for (Issue issue : outcome.getIssues()) {
                BadRequest.FieldViolation.Builder violation = BadRequest.FieldViolation.newBuilder()
                    .setDescription(issue.getMessage());
                if (issue.getPath() != null) {
                    violation.setField(issue.getPath());
                }
                if (issue.getCode() != null) {
                    violation.setReason(issue.getCode().getName());
                }
                request.addFieldViolations(violation);
            }
            status.addDetails(Any.pack(request.build()));
        }

        return status.build();
    }
}

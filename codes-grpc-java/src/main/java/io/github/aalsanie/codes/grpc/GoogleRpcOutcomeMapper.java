package io.github.aalsanie.codes.grpc;

import com.google.protobuf.Any;
import com.google.rpc.BadRequest;
import com.google.rpc.DebugInfo;
import com.google.rpc.ErrorInfo;
import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeCode;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Maps failed Codes outcomes to {@code google.rpc.Status}.
 *
 * <p>The Codes identity is represented losslessly as {@link ErrorInfo#getDomain() domain} =
 * {@link OutcomeCode#getNamespace() namespace} and {@link ErrorInfo#getReason() reason} =
 * {@link OutcomeCode#getName() name}. Codes never truncates or normalizes identity to make it fit
 * the Google RPC contract. A mapped outcome whose name cannot be represented as an
 * {@code ErrorInfo.reason} is rejected.
 *
 * <p>Structured issues are exposed through {@link BadRequest} only for gRPC
 * {@code INVALID_ARGUMENT} and {@code OUT_OF_RANGE}. Every exposed issue must have a path that the
 * application intends as a request-field path. Pathless issues cannot be represented honestly as
 * {@link BadRequest.FieldViolation} and are rejected instead of being emitted with an empty field.
 *
 * <p>When a coded {@link Issue} is exposed through {@link BadRequest.FieldViolation#getReason()
 * reason}, its namespace must match the enclosing outcome namespace. This keeps the field-level
 * reason scoped by the same {@code ErrorInfo.domain} and avoids silently changing issue identity.
 */
public final class GoogleRpcOutcomeMapper {
    private static final int MAX_GOOGLE_REASON_LENGTH = 63;
    private static final String GOOGLE_REASON_EXPRESSION = "[A-Z][A-Z0-9_]+[A-Z0-9]";
    private static final Pattern GOOGLE_REASON_PATTERN =
        Pattern.compile(GOOGLE_REASON_EXPRESSION);

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
            throw new IllegalArgumentException(
                "google.rpc.Status error details are only produced for failed outcomes"
            );
        }

        return mapper.map(outcome).fold(
            status -> MappingResult.mapped(createStatus(outcome, status)),
            MappingResult::unmapped
        );
    }

    private com.google.rpc.Status createStatus(Outcome outcome, GrpcStatusCode statusCode) {
        OutcomeCode outcomeCode = outcome.getCode();
        requireGoogleReason(outcomeCode, "outcome code", "google.rpc.ErrorInfo.reason");

        com.google.rpc.Status.Builder status = com.google.rpc.Status.newBuilder()
            .setCode(statusCode.getValue())
            .setMessage(exposure.exposeMessage() ? outcome.getMessage() : outcomeCode.getValue())
            .addDetails(Any.pack(
                ErrorInfo.newBuilder()
                    .setDomain(outcomeCode.getNamespace())
                    .setReason(outcomeCode.getName())
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
            status.addDetails(Any.pack(toBadRequest(outcome, statusCode)));
        }

        return status.build();
    }

    private static BadRequest toBadRequest(Outcome outcome, GrpcStatusCode statusCode) {
        requireBadRequestStatus(outcome, statusCode);

        OutcomeCode outcomeCode = outcome.getCode();
        BadRequest.Builder request = BadRequest.newBuilder();

        for (Issue issue : outcome.getIssues()) {
            String path = issue.getPath();
            if (path == null) {
                throw new IllegalArgumentException(
                    "issue on outcome '" + outcomeCode.getValue()
                        + "' cannot be represented as google.rpc.BadRequest.FieldViolation"
                        + "; exposed gRPC issues must have a request-field path"
                );
            }

            BadRequest.FieldViolation.Builder violation = BadRequest.FieldViolation.newBuilder()
                .setField(path)
                .setDescription(issue.getMessage());

            OutcomeCode issueCode = issue.getCode();
            if (issueCode != null) {
                requireFieldViolationIdentity(outcomeCode, issueCode);
                violation.setReason(issueCode.getName());
            }

            request.addFieldViolations(violation);
        }

        return request.build();
    }

    private static void requireBadRequestStatus(Outcome outcome, GrpcStatusCode statusCode) {
        if (statusCode == GrpcStatusCode.INVALID_ARGUMENT
            || statusCode == GrpcStatusCode.OUT_OF_RANGE) {
            return;
        }

        throw new IllegalArgumentException(
            "issues on outcome '" + outcome.getCode().getValue()
                + "' cannot be represented as google.rpc.BadRequest when mapped to gRPC "
                + statusCode
                + "; BadRequest issue exposure requires INVALID_ARGUMENT or OUT_OF_RANGE"
        );
    }

    private static void requireFieldViolationIdentity(
        OutcomeCode outcomeCode,
        OutcomeCode issueCode
    ) {
        if (!outcomeCode.getNamespace().equals(issueCode.getNamespace())) {
            throw new IllegalArgumentException(
                "issue code '" + issueCode.getValue()
                    + "' cannot be represented losslessly as google.rpc.BadRequest.FieldViolation"
                    + ".reason under ErrorInfo.domain '" + outcomeCode.getNamespace()
                    + "'; coded issues must use the same namespace as the enclosing outcome"
            );
        }

        requireGoogleReason(
            issueCode,
            "issue code",
            "google.rpc.BadRequest.FieldViolation.reason"
        );
    }

    private static void requireGoogleReason(
        OutcomeCode code,
        String label,
        String target
    ) {
        String reason = code.getName();
        if (reason.length() <= MAX_GOOGLE_REASON_LENGTH
            && GOOGLE_REASON_PATTERN.matcher(reason).matches()) {
            return;
        }

        throw new IllegalArgumentException(
            label + " '" + code.getValue() + "' cannot be represented losslessly as " + target
                + "; reason must be at most " + MAX_GOOGLE_REASON_LENGTH
                + " characters and match " + GOOGLE_REASON_EXPRESSION
        );
    }
}

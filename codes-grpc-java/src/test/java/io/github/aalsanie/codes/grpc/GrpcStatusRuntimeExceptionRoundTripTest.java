package io.github.aalsanie.codes.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.BadRequest;
import com.google.rpc.DebugInfo;
import com.google.rpc.ErrorInfo;
import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeCode;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GrpcStatusRuntimeExceptionRoundTripTest {
    private static final String APP_NAMESPACE = "com.example.checkout";
    private static final String PROTECTED_DETAIL = "gateway_token=secret-123";
    private static final String PROTECTED_ISSUE_MESSAGE = "Payment method is invalid.";

    private static final OutcomeDefinition PAYMENT_DECLINED = OutcomeDefinition.custom(
        APP_NAMESPACE,
        "PAYMENT_DECLINED",
        OutcomeState.FAILED,
        "The payment was declined."
    );

    private static final OutcomeCode PAYMENT_METHOD_INVALID = OutcomeCode.of(
        APP_NAMESPACE,
        "PAYMENT_METHOD_INVALID"
    );

    private static final GrpcOutcomeMapper APP_MAPPER = GrpcOutcomeMapper.standard()
        .withMapping(PAYMENT_DECLINED, GrpcStatusCode.FAILED_PRECONDITION);

    @Test
    void decodedStatusRuntimeExceptionsMatchGoldenContract() throws Exception {
        Outcome outcome = testOutcome();

        com.google.rpc.Status safe = roundTrip(
            outcome,
            new GoogleRpcOutcomeMapper(APP_MAPPER, GrpcOutcomeExposure.safeDefaults())
        );
        com.google.rpc.Status publicErrors = roundTrip(
            outcome,
            new GoogleRpcOutcomeMapper(APP_MAPPER, GrpcOutcomeExposure.publicErrors())
        );
        com.google.rpc.Status explicit = roundTrip(
            outcome,
            new GoogleRpcOutcomeMapper(
                APP_MAPPER,
                new GrpcOutcomeExposure(true, true, true)
            )
        );

        assertIdentity(safe, outcome.getCode());
        assertIdentity(publicErrors, outcome.getCode());
        assertIdentity(explicit, outcome.getCode());

        assertProtectedPayloadAbsent(safe);
        assertProtectedDetailAbsent(publicErrors);
        assertEquals(PROTECTED_DETAIL, debugInfo(explicit).orElseThrow().getDetail());

        String actual = String.join(
            "\n\n",
            snapshot("safe", safe),
            snapshot("public", publicErrors),
            snapshot("explicit", explicit)
        );

        String expected = Files.readString(
            Path.of(System.getProperty("codes.grpcStatusSnapshot"))
        ).replace("\r\n", "\n").stripTrailing();

        assertEquals(expected, actual);
    }

    private static com.google.rpc.Status roundTrip(
        Outcome outcome,
        GoogleRpcOutcomeMapper mapper
    ) {
        StatusRuntimeException exception = GrpcOutcomeExceptions
            .toStatusRuntimeException(outcome, mapper)
            .orNull();

        assertNotNull(exception);
        assertEquals(
            io.grpc.Status.Code.FAILED_PRECONDITION,
            exception.getStatus().getCode()
        );

        com.google.rpc.Status decoded = StatusProto.fromThrowable(exception);
        assertNotNull(decoded);
        return decoded;
    }

    private static void assertIdentity(
        com.google.rpc.Status status,
        OutcomeCode expected
    ) throws InvalidProtocolBufferException {
        ErrorInfo info = errorInfo(status);
        OutcomeCode decoded = OutcomeCode.of(info.getDomain(), info.getReason());
        assertEquals(expected, decoded);
    }

    private static void assertProtectedPayloadAbsent(com.google.rpc.Status status) {
        assertEquals(1, status.getDetailsCount());
        assertTrue(debugInfo(status).isEmpty());
        assertTrue(badRequest(status).isEmpty());

        String serialized = new String(
            status.toByteArray(),
            StandardCharsets.ISO_8859_1
        );
        assertFalse(serialized.contains(PROTECTED_DETAIL));
        assertFalse(serialized.contains(PROTECTED_ISSUE_MESSAGE));
    }

    private static void assertProtectedDetailAbsent(com.google.rpc.Status status) {
        assertTrue(debugInfo(status).isEmpty());

        String serialized = new String(
            status.toByteArray(),
            StandardCharsets.ISO_8859_1
        );
        assertFalse(serialized.contains(PROTECTED_DETAIL));
    }

    private static String snapshot(
        String name,
        com.google.rpc.Status status
    ) throws InvalidProtocolBufferException {
        ErrorInfo info = errorInfo(status);
        Optional<DebugInfo> debugInfo = debugInfo(status);
        Optional<BadRequest> badRequest = badRequest(status);

        StringBuilder result = new StringBuilder();
        result.append('[').append(name).append("]\n")
            .append("code=").append(status.getCode()).append('\n')
            .append("message=").append(status.getMessage()).append('\n')
            .append("details=").append(status.getDetailsCount()).append('\n')
            .append("identity.domain=").append(info.getDomain()).append('\n')
            .append("identity.reason=").append(info.getReason()).append('\n')
            .append("debug=")
            .append(debugInfo.map(DebugInfo::getDetail).orElse("<absent>"))
            .append('\n');

        if (badRequest.isEmpty()) {
            result.append("issues=0");
            return result.toString();
        }

        BadRequest request = badRequest.orElseThrow();
        result.append("issues=").append(request.getFieldViolationsCount());

        for (int index = 0; index < request.getFieldViolationsCount(); index++) {
            BadRequest.FieldViolation violation = request.getFieldViolations(index);
            result.append('\n')
                .append("issue.").append(index).append(".field=")
                .append(orAbsent(violation.getField()))
                .append('\n')
                .append("issue.").append(index).append(".reason=")
                .append(orAbsent(violation.getReason()))
                .append('\n')
                .append("issue.").append(index).append(".description=")
                .append(violation.getDescription());
        }

        return result.toString();
    }

    private static ErrorInfo errorInfo(
        com.google.rpc.Status status
    ) throws InvalidProtocolBufferException {
        for (Any detail : status.getDetailsList()) {
            if (detail.is(ErrorInfo.class)) {
                return detail.unpack(ErrorInfo.class);
            }
        }
        throw new IllegalStateException("decoded google.rpc.Status is missing ErrorInfo");
    }

    private static Optional<DebugInfo> debugInfo(com.google.rpc.Status status) {
        for (Any detail : status.getDetailsList()) {
            if (detail.is(DebugInfo.class)) {
                try {
                    return Optional.of(detail.unpack(DebugInfo.class));
                } catch (InvalidProtocolBufferException exception) {
                    throw new IllegalStateException("invalid DebugInfo detail", exception);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<BadRequest> badRequest(com.google.rpc.Status status) {
        for (Any detail : status.getDetailsList()) {
            if (detail.is(BadRequest.class)) {
                try {
                    return Optional.of(detail.unpack(BadRequest.class));
                } catch (InvalidProtocolBufferException exception) {
                    throw new IllegalStateException("invalid BadRequest detail", exception);
                }
            }
        }
        return Optional.empty();
    }

    private static String orAbsent(String value) {
        return value.isEmpty() ? "<absent>" : value;
    }

    private static Outcome testOutcome() {
        return Outcome.of(
            PAYMENT_DECLINED,
            PROTECTED_DETAIL,
            List.of(
                Issue.at(
                    "paymentMethod",
                    PAYMENT_METHOD_INVALID,
                    PROTECTED_ISSUE_MESSAGE
                ),
                Issue.at("amount", "Amount must be positive.")
            )
        );
    }
}

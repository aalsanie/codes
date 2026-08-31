package io.github.aalsanie.codes.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.rpc.BadRequest;
import com.google.rpc.DebugInfo;
import com.google.rpc.ErrorInfo;
import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import java.util.List;
import org.junit.jupiter.api.Test;

class GoogleRpcOutcomeMapperTest {
    @Test
    void safeDefaultsExposeMachineIdentityWithoutOccurrenceDetail() throws Exception {
        Outcome outcome = Outcome.of(
            StandardOutcomes.INVALID_ARGUMENT,
            "request=42",
            List.of(Issue.at("email", "Invalid email."))
        );
        com.google.rpc.Status status = GoogleRpcOutcomeMapper.safeDefaults().map(outcome).orNull();
        assertEquals(3, status.getCode());
        assertEquals(outcome.getCode().getValue(), status.getMessage());
        assertEquals(1, status.getDetailsCount());
        ErrorInfo info = status.getDetails(0).unpack(ErrorInfo.class);
        assertEquals(outcome.getCode().getNamespace(), info.getDomain());
        assertEquals(outcome.getCode().getName(), info.getReason());
    }

    @Test
    void explicitExposureAddsMessageDebugDetailAndFieldViolations() throws Exception {
        Outcome outcome = Outcome.of(
            StandardOutcomes.INVALID_ARGUMENT,
            "request=42",
            List.of(Issue.at("email", "Invalid email."))
        );
        GoogleRpcOutcomeMapper mapper = new GoogleRpcOutcomeMapper(
            io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper.standard(),
            new GrpcOutcomeExposure(true, true, true)
        );
        com.google.rpc.Status status = mapper.map(outcome).orNull();
        assertEquals(outcome.getMessage(), status.getMessage());
        assertEquals(3, status.getDetailsCount());
        DebugInfo debug = status.getDetails(1).unpack(DebugInfo.class);
        assertEquals("request=42", debug.getDetail());
        BadRequest request = status.getDetails(2).unpack(BadRequest.class);
        assertEquals("email", request.getFieldViolations(0).getField());
        assertEquals("Invalid email.", request.getFieldViolations(0).getDescription());
    }

    @Test
    void mapsToGrpcStatusRuntimeException() {
        Outcome outcome = Outcome.of(StandardOutcomes.NOT_FOUND);
        StatusRuntimeException exception = GrpcOutcomeExceptions
            .toStatusRuntimeException(outcome, GoogleRpcOutcomeMapper.safeDefaults())
            .orNull();
        assertEquals(io.grpc.Status.Code.NOT_FOUND, exception.getStatus().getCode());
        assertEquals(outcome.getCode().getValue(), StatusProto.fromThrowable(exception).getMessage());
    }


    @Test
    void richErrorMapperRejectsSuccessfulOutcomes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> GoogleRpcOutcomeMapper.safeDefaults().map(Outcome.of(StandardOutcomes.OK))
        );
    }

    @Test
    void exposurePoliciesAreExplicit() {
        assertEquals(new GrpcOutcomeExposure(false, false, false), GrpcOutcomeExposure.safeDefaults());
        assertEquals(new GrpcOutcomeExposure(true, false, true), GrpcOutcomeExposure.publicErrors());
        assertFalse(GrpcOutcomeExposure.safeDefaults().exposeIssues());
        assertTrue(GrpcOutcomeExposure.publicErrors().exposeIssues());
    }
}

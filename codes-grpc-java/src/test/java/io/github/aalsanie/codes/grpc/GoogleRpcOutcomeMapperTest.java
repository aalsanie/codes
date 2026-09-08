package io.github.aalsanie.codes.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.List;
import org.junit.jupiter.api.Test;

class GoogleRpcOutcomeMapperTest {
    private static final String APP_NAMESPACE = "com.example.checkout";

    private static final OutcomeDefinition PAYMENT_DECLINED = OutcomeDefinition.custom(
        APP_NAMESPACE,
        "PAYMENT_DECLINED",
        OutcomeState.FAILED,
        "The payment was declined."
    );

    private static final OutcomeDefinition CHECKOUT_INVALID = OutcomeDefinition.custom(
        APP_NAMESPACE,
        "CHECKOUT_INVALID",
        OutcomeState.FAILED,
        "Checkout request is invalid."
    );

    private static final OutcomeDefinition RANGE_INVALID = OutcomeDefinition.custom(
        APP_NAMESPACE,
        "RANGE_INVALID",
        OutcomeState.FAILED,
        "Requested range is invalid."
    );

    private static final OutcomeCode PAYMENT_METHOD_INVALID = OutcomeCode.of(
        APP_NAMESPACE,
        "PAYMENT_METHOD_INVALID"
    );

    private static final GrpcOutcomeMapper APP_MAPPER = GrpcOutcomeMapper.standard()
        .withMapping(PAYMENT_DECLINED, GrpcStatusCode.FAILED_PRECONDITION)
        .withMapping(CHECKOUT_INVALID, GrpcStatusCode.INVALID_ARGUMENT)
        .withMapping(RANGE_INVALID, GrpcStatusCode.OUT_OF_RANGE);

    @Test
    void safeDefaultsExposeOnlyMachineIdentity() throws Exception {
        Outcome outcome = paymentDeclinedOutcome();
        GoogleRpcOutcomeMapper mapper = mapper(GrpcOutcomeExposure.safeDefaults());

        com.google.rpc.Status status = mapper.map(outcome).orNull();

        assertNotNull(status);
        assertEquals(9, status.getCode());
        assertEquals(outcome.getCode().getValue(), status.getMessage());
        assertEquals(1, status.getDetailsCount());

        ErrorInfo info = status.getDetails(0).unpack(ErrorInfo.class);
        assertEquals(outcome.getCode().getNamespace(), info.getDomain());
        assertEquals(outcome.getCode().getName(), info.getReason());
    }

    @Test
    void publicErrorsExposeRequestIssuesButNotOccurrenceDetail() throws Exception {
        Outcome outcome = validationOutcome();
        GoogleRpcOutcomeMapper mapper = mapper(GrpcOutcomeExposure.publicErrors());

        com.google.rpc.Status status = mapper.map(outcome).orNull();

        assertNotNull(status);
        assertEquals(3, status.getCode());
        assertEquals(outcome.getMessage(), status.getMessage());
        assertEquals(2, status.getDetailsCount());

        ErrorInfo info = status.getDetails(0).unpack(ErrorInfo.class);
        assertEquals(APP_NAMESPACE, info.getDomain());
        assertEquals("CHECKOUT_INVALID", info.getReason());

        BadRequest request = status.getDetails(1).unpack(BadRequest.class);
        assertEquals(2, request.getFieldViolationsCount());

        BadRequest.FieldViolation coded = request.getFieldViolations(0);
        assertEquals("paymentMethod", coded.getField());
        assertEquals("PAYMENT_METHOD_INVALID", coded.getReason());
        assertEquals("Payment method is invalid.", coded.getDescription());

        BadRequest.FieldViolation uncoded = request.getFieldViolations(1);
        assertEquals("amount", uncoded.getField());
        assertEquals("", uncoded.getReason());
        assertEquals("Amount must be positive.", uncoded.getDescription());
    }

    @Test
    void explicitDetailExposureAddsDebugInfo() throws Exception {
        Outcome outcome = validationOutcome();
        GoogleRpcOutcomeMapper mapper = mapper(new GrpcOutcomeExposure(true, true, true));

        com.google.rpc.Status status = mapper.map(outcome).orNull();

        assertNotNull(status);
        assertEquals(3, status.getDetailsCount());
        DebugInfo debugInfo = status.getDetails(1).unpack(DebugInfo.class);
        assertEquals("gateway_token=secret-123", debugInfo.getDetail());
        assertTrue(status.getDetails(2).is(BadRequest.class));
    }

    @Test
    void outOfRangeCanExposeBadRequest() throws Exception {
        Outcome outcome = Outcome.of(
            RANGE_INVALID,
            null,
            List.of(Issue.at("offset", "Offset is outside the available range."))
        );

        com.google.rpc.Status status = mapper(GrpcOutcomeExposure.publicErrors())
            .map(outcome)
            .orNull();

        assertNotNull(status);
        assertEquals(11, status.getCode());
        BadRequest request = status.getDetails(1).unpack(BadRequest.class);
        assertEquals("offset", request.getFieldViolations(0).getField());
    }

    @Test
    void rejectsBadRequestIssuesForFailedPrecondition() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper(GrpcOutcomeExposure.publicErrors()).map(paymentDeclinedOutcome())
        );

        assertTrue(exception.getMessage().contains("FAILED_PRECONDITION"));
        assertTrue(exception.getMessage().contains("INVALID_ARGUMENT or OUT_OF_RANGE"));
    }

    @Test
    void rejectsPathlessIssueAsBadRequestFieldViolation() {
        Outcome outcome = Outcome.of(
            CHECKOUT_INVALID,
            null,
            List.of(Issue.of("The request is invalid."))
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper(GrpcOutcomeExposure.publicErrors()).map(outcome)
        );

        assertTrue(exception.getMessage().contains("request-field path"));
        assertTrue(exception.getMessage().contains(CHECKOUT_INVALID.getCode().getValue()));
    }

    @Test
    void rejectsOutcomeNamesLongerThanErrorInfoReasonAllows() {
        OutcomeDefinition incompatible = OutcomeDefinition.custom(
            "com.example.compatibility",
            "A".repeat(64),
            OutcomeState.FAILED,
            "Incompatible reason."
        );
        GoogleRpcOutcomeMapper mapper = mapperFor(
            incompatible,
            GrpcOutcomeExposure.safeDefaults()
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper.map(Outcome.of(incompatible))
        );

        assertTrue(exception.getMessage().contains("at most 63"));
        assertTrue(exception.getMessage().contains(incompatible.getCode().getValue()));
    }

    @Test
    void rejectsTrailingUnderscoreThatCannotBeErrorInfoReason() {
        OutcomeDefinition incompatible = OutcomeDefinition.custom(
            "com.example.compatibility",
            "PAYMENT_DECLINED_",
            OutcomeState.FAILED,
            "Incompatible reason."
        );
        GoogleRpcOutcomeMapper mapper = mapperFor(
            incompatible,
            GrpcOutcomeExposure.safeDefaults()
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper.map(Outcome.of(incompatible))
        );

        assertTrue(exception.getMessage().contains("[A-Z][A-Z0-9_]+[A-Z0-9]"));
    }

    @Test
    void rejectsCodedIssueFromDifferentDomainInsteadOfChangingItsIdentity() {
        OutcomeCode differentDomain = OutcomeCode.of(
            "com.example.validation",
            "PAYMENT_METHOD_INVALID"
        );
        Outcome outcome = Outcome.of(
            CHECKOUT_INVALID,
            null,
            List.of(
                Issue.at(
                    "paymentMethod",
                    differentDomain,
                    "Payment method is invalid."
                )
            )
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> mapper(GrpcOutcomeExposure.publicErrors()).map(outcome)
        );

        assertTrue(exception.getMessage().contains("same namespace"));
        assertTrue(exception.getMessage().contains(differentDomain.getValue()));
    }

    @Test
    void richErrorMapperRejectsSuccessfulOutcomes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> GoogleRpcOutcomeMapper.safeDefaults().map(
                Outcome.of(io.github.aalsanie.codes.StandardOutcomes.OK)
            )
        );
    }

    @Test
    void exposurePoliciesAreExplicit() {
        assertEquals(
            new GrpcOutcomeExposure(false, false, false),
            GrpcOutcomeExposure.safeDefaults()
        );
        assertEquals(
            new GrpcOutcomeExposure(true, false, true),
            GrpcOutcomeExposure.publicErrors()
        );
        assertFalse(GrpcOutcomeExposure.safeDefaults().exposeIssues());
        assertTrue(GrpcOutcomeExposure.publicErrors().exposeIssues());
    }

    private static Outcome validationOutcome() {
        return Outcome.of(
            CHECKOUT_INVALID,
            "gateway_token=secret-123",
            List.of(
                Issue.at(
                    "paymentMethod",
                    PAYMENT_METHOD_INVALID,
                    "Payment method is invalid."
                ),
                Issue.at("amount", "Amount must be positive.")
            )
        );
    }

    private static Outcome paymentDeclinedOutcome() {
        return Outcome.of(
            PAYMENT_DECLINED,
            "gateway_token=secret-123",
            List.of(Issue.of("Payment cannot be completed."))
        );
    }

    private static GoogleRpcOutcomeMapper mapper(GrpcOutcomeExposure exposure) {
        return new GoogleRpcOutcomeMapper(APP_MAPPER, exposure);
    }

    private static GoogleRpcOutcomeMapper mapperFor(
        OutcomeDefinition definition,
        GrpcOutcomeExposure exposure
    ) {
        return new GoogleRpcOutcomeMapper(
            GrpcOutcomeMapper.standard()
                .withMapping(definition, GrpcStatusCode.INTERNAL),
            exposure
        );
    }
}

package io.github.aalsanie.codes;

import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class JavaInteropTest {
    @Test
    void createsAndMapsOutcomeFromJava() {
        Outcome outcome = Outcome.of(StandardOutcomes.NOT_FOUND, "customer 123", List.of());
        require(outcome.isFailed());
        require(StandardOutcomes.NOT_FOUND.getDefaultMessage().equals(outcome.getMessage()));
        require(HttpStatusCode.NOT_FOUND.equals(HttpOutcomeMapper.standard().map(outcome).orNull()));
        require(GrpcStatusCode.NOT_FOUND.equals(GrpcOutcomeMapper.standard().map(outcome).orNull()));
    }

    @Test
    void standardCollectionsCannotBeMutatedFromJava() {
        expectUnsupported(() -> StandardOutcomes.all.clear());
        expectUnsupported(() -> OutcomeRegistry.standard().definitions().clear());
        expectUnsupported(() -> HttpOutcomeMapper.standard().mappings().clear());
    }

    @Test
    void outcomeDefensivelyCopiesMutableJavaList() {
        ArrayList<Issue> source = new ArrayList<>();
        source.add(Issue.of("Bad."));
        Outcome outcome = Outcome.of(StandardOutcomes.INTERNAL, null, source);
        source.clear();
        require(outcome.getIssues().size() == 1);
        expectUnsupported(() -> outcome.getIssues().clear());
    }

    @Test
    void customDefinitionCannotClaimLibraryNamespace() {
        expectIllegalArgument(() -> OutcomeDefinition.custom(
            StandardOutcomes.NAMESPACE,
            "OK",
            OutcomeState.FAILED,
            "Forged."
        ));
    }

    @Test
    void validationResultIsUsableFromJava() {
        ValidationResult result = ValidationResult.combine(List.of(
            ValidationResult.valid(),
            ValidationResult.invalid(Issue.at("email", "Invalid."))
        ));
        require(result.isInvalid());
        require(result.issues().size() == 1);
        require(result.toOutcome().getDefinition() == StandardOutcomes.INVALID_ARGUMENT);
    }

    @Test
    void outcomeCodeRoundTripsFromJava() {
        OutcomeCode code = OutcomeCode.of("com.example", "CODE");
        require(code.equals(OutcomeCode.parse(code.getValue())));
        require(OutcomeCode.parseOrNull("bad") == null);
    }

    private static void require(boolean value) {
        if (!value) throw new AssertionError("condition failed");
    }

    private static void expectUnsupported(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
        }
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
}

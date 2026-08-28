import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeCode;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeRegistry;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.ValidationResult;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;

public final class SmokeJava {

    private SmokeJava() {
    }

    public static void main(String[] args) {
        createsAndMapsOutcome();
        validatesFromJava();
        checksDefensiveCopying();
        createsCustomDefinition();
        roundTripsOutcomeCode();
        checksRegistry();

        System.out.println("Pure Java consumer smoke test passed.");
    }

    private static void createsAndMapsOutcome() {
        Outcome outcome =
            Outcome.of(
                StandardOutcomes.NOT_FOUND,
                "customer 123",
                List.of()
            );

        require(outcome.isFailed());

        require(
            StandardOutcomes.NOT_FOUND
                .getDefaultMessage()
                .equals(outcome.getMessage())
        );

        require(
            HttpStatusCode.NOT_FOUND.equals(
                HttpOutcomeMapper.standard()
                    .map(outcome)
                    .orNull()
            )
        );

        require(
            GrpcStatusCode.NOT_FOUND.equals(
                GrpcOutcomeMapper.standard()
                    .map(outcome)
                    .orNull()
            )
        );
    }

    private static void validatesFromJava() {
        ValidationResult result =
            ValidationResult.combine(
                List.of(
                    ValidationResult.valid(),
                    ValidationResult.invalid(
                        Issue.at(
                            "email",
                            "Invalid."
                        )
                    )
                )
            );

        require(result.isInvalid());
        require(result.issues().size() == 1);

        require(
            result.toOutcome(StandardOutcomes.INVALID_ARGUMENT)
                .getDefinition()
                == StandardOutcomes.INVALID_ARGUMENT
        );
    }

    private static void checksDefensiveCopying() {
        ArrayList<Issue> source =
            new ArrayList<>();

        source.add(
            Issue.of("Bad.")
        );

        Outcome outcome =
            Outcome.of(
                StandardOutcomes.INTERNAL,
                null,
                source
            );

        source.clear();

        require(outcome.getIssues().size() == 1);
    }

    private static void createsCustomDefinition() {
        OutcomeDefinition definition =
            OutcomeDefinition.custom(
                "com.example.orders",
                "ORDER_REJECTED",
                OutcomeState.FAILED,
                "Order rejected."
            );

        require(definition.getState() == OutcomeState.FAILED);
    }

    private static void roundTripsOutcomeCode() {
        OutcomeCode code =
            OutcomeCode.of(
                "com.example",
                "CODE"
            );

        require(
            code.equals(
                OutcomeCode.parse(
                    code.getValue()
                )
            )
        );
    }

    private static void checksRegistry() {
        require(
            !OutcomeRegistry.standard()
                .definitions()
                .isEmpty()
        );
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new AssertionError(
                "Pure Java consumer assertion failed."
            );
        }
    }
}

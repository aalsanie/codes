import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.ValidationResult;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;

public final class SmokeJava {
    private SmokeJava() {
    }

    public static void main(String[] args) {
        require(HttpStatusCode.NOT_FOUND.equals(HttpOutcomeMapper.standard().map(StandardOutcomes.NOT_FOUND).orNull()));
        require(GrpcStatusCode.NOT_FOUND.equals(GrpcOutcomeMapper.standard().map(StandardOutcomes.NOT_FOUND).orNull()));

        OutcomeDefinition custom = OutcomeDefinition.custom(
            "com.example.orders",
            "ORDER_REJECTED",
            OutcomeState.FAILED,
            "The order was rejected."
        );
        Outcome outcome = Outcome.of(custom, "runtime detail", ListFactory.emptyIssues());
        require(outcome.getMessage().equals("The order was rejected."));

        ValidationResult result = ValidationResult.invalid(Issue.at("quantity", "Quantity must be positive."));
        require(result.toOutcome().getDefinition() == StandardOutcomes.INVALID_ARGUMENT);
    }

    private static void require(boolean value) {
        if (!value) {
            throw new AssertionError("smoke assertion failed");
        }
    }

    private static final class ListFactory {
        private ListFactory() {
        }

        private static java.util.List<Issue> emptyIssues() {
            return java.util.List.of();
        }
    }
}

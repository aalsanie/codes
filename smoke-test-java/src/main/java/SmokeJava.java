import com.google.rpc.ErrorInfo;
import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.OutcomeCode;
import io.github.aalsanie.codes.OutcomeDefinition;
import io.github.aalsanie.codes.OutcomeRegistry;
import io.github.aalsanie.codes.OutcomeState;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.ValidationResult;
import io.github.aalsanie.codes.grpc.GoogleRpcOutcomeMapper;
import io.github.aalsanie.codes.grpc.GrpcOutcomeExceptions;
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpStatusCode;
import io.github.aalsanie.codes.spring.OutcomeProblemDetailMapper;
import io.github.aalsanie.codes.spring.SpringOutcomeExceptions;
import io.grpc.StatusRuntimeException;
import java.util.List;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public final class SmokeJava {
    private SmokeJava() {
    }

    public static void main(String[] args) throws Exception {
        OutcomeCode code = OutcomeCode.of("com.example.orders", "ORDER_REJECTED");
        OutcomeDefinition rejected = OutcomeDefinition.custom(
            code,
            OutcomeState.FAILED,
            "Order rejected."
        );
        Outcome outcome = Outcome.of(
            rejected,
            "internal=42",
            List.of(Issue.at("items[0]", "Unavailable."))
        );

        HttpOutcomeMapper http = HttpOutcomeMapper.standard()
            .withMapping(rejected, HttpStatusCode.of(422));
        GrpcOutcomeMapper grpc = GrpcOutcomeMapper.standard()
            .withMapping(rejected, GrpcStatusCode.FAILED_PRECONDITION);
        require(http.map(outcome).orNull().getValue() == 422);
        require(grpc.map(outcome).orNull() == GrpcStatusCode.FAILED_PRECONDITION);

        Outcome standardFailure = Outcome.of(StandardOutcomes.NOT_FOUND);

        ProblemDetail problem = OutcomeProblemDetailMapper.safeDefaults()
            .map(standardFailure)
            .orNull();
        require(problem.getStatus() == 404);

        ErrorResponseException springException = SpringOutcomeExceptions
            .toErrorResponseException(
                standardFailure,
                OutcomeProblemDetailMapper.safeDefaults()
            )
            .orNull();
        require(springException.getStatusCode().value() == 404);

        com.google.rpc.Status rpcStatus = GoogleRpcOutcomeMapper.safeDefaults()
            .map(standardFailure)
            .orNull();
        require(rpcStatus.getCode() == io.grpc.Status.Code.NOT_FOUND.value());

        ErrorInfo identity = rpcStatus.getDetails(0).unpack(ErrorInfo.class);
        require(identity.getDomain().equals(standardFailure.getCode().getNamespace()));
        require(identity.getReason().equals(standardFailure.getCode().getName()));

        StatusRuntimeException grpcException = GrpcOutcomeExceptions
            .toStatusRuntimeException(
                standardFailure,
                GoogleRpcOutcomeMapper.safeDefaults()
            )
            .orNull();
        require(grpcException.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND);

        ValidationResult validation = ValidationResult.combine(
            ValidationResult.valid(),
            ValidationResult.invalid(Issue.at("email", "Invalid."))
        );
        require(validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT).getIssues().size() == 1);

        OutcomeRegistry registry = OutcomeRegistry.standard().with(rejected);
        require(registry.require(code) == rejected);
        MappingResult<Integer> mapped = MappingResult.mapped(42);
        require(mapped.fold(value -> value, () -> -1) == 42);

        System.out.println("Pure Java consumer smoke test passed for all Codes artifacts.");
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new AssertionError("Pure Java consumer assertion failed.");
        }
    }
}

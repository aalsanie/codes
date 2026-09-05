import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.grpc.GoogleRpcOutcomeMapper;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;
import io.github.aalsanie.codes.spring.OutcomeProblemDetailMapper;

public final class SmokeMavenJava {
    private SmokeMavenJava() {
    }

    public static void main(String[] args) {
        Outcome outcome = Outcome.of(StandardOutcomes.NOT_FOUND);
        if (HttpOutcomeMapper.standard().map(outcome).orNull().getValue() != 404) {
            throw new AssertionError("Maven Java consumer mapping failed");
        }
        if (OutcomeProblemDetailMapper.safeDefaults().map(outcome).orNull().getStatus() != 404) {
            throw new AssertionError("Maven Java Spring adapter mapping failed");
        }
        if (GoogleRpcOutcomeMapper.safeDefaults().map(outcome).orNull().getCode()
            != io.grpc.Status.Code.NOT_FOUND.value()) {
            throw new AssertionError("Maven Java gRPC adapter mapping failed");
        }
        System.out.println("Maven Java consumer smoke test passed for all Codes artifacts.");
    }
}

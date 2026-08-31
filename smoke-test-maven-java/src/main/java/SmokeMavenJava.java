import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper;

public final class SmokeMavenJava {
    private SmokeMavenJava() {
    }

    public static void main(String[] args) {
        if (HttpOutcomeMapper.standard().map(Outcome.of(StandardOutcomes.NOT_FOUND)).orNull().getValue() != 404) {
            throw new AssertionError("Maven Java consumer mapping failed");
        }
        System.out.println("Maven Java consumer smoke test passed.");
    }
}

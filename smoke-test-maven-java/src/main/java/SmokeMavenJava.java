import io.github.aalsanie.codes.ProblemType;
import io.github.aalsanie.codes.spring.ProblemDetails;
import java.net.URI;
import org.springframework.http.ProblemDetail;

public final class SmokeMavenJava {
    private SmokeMavenJava() {
    }

    public static void main(String[] args) {
        ProblemType type = ProblemType.of(
            URI.create("https://api.example.com/problems/order-not-found"),
            404,
            "Order not found"
        );
        ProblemDetail problem = ProblemDetails.forType(type);
        if (!type.getType().equals(problem.getType()) || problem.getStatus() != 404) {
            throw new AssertionError("Maven Java consumer mapping failed");
        }
    }
}

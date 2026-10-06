import io.github.aalsanie.codes.ProblemType;
import io.github.aalsanie.codes.spring.ProblemDetails;
import java.net.URI;
import org.springframework.http.ProblemDetail;

public final class SmokeJava {
    private SmokeJava() {
    }

    public static void main(String[] args) {
        ProblemType type = ProblemType.of(
            URI.create("https://api.example.com/problems/order-not-found"),
            404,
            "Order not found"
        );
        ProblemDetail problem = ProblemDetails.forTypeAndDetail(
            type,
            "Order o-123 was not found."
        );

        require(problem.getType().equals(type.getType()));
        require(problem.getStatus() == 404);
        require(problem.getTitle().equals("Order not found"));
        require(problem.getDetail().equals("Order o-123 was not found."));
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new AssertionError("Java consumer assertion failed.");
        }
    }
}

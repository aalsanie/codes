import io.github.aalsanie.codes.ProblemType
import io.github.aalsanie.codes.spring.ProblemDetails
import java.net.URI

fun main() {
    val type = ProblemType.of(
        URI.create("https://api.example.com/problems/order-not-found"),
        404,
        "Order not found",
    )
    val problem = ProblemDetails.forType(type)

    check(problem.type == type.type)
    check(problem.status == 404)
}

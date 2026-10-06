import io.github.aalsanie.codes.ProblemType
import io.github.aalsanie.codes.spring.ProblemDetails
import java.net.URI

fun main() {
    val type = ProblemType.of(
        URI.create("https://api.example.com/problems/order-not-found"),
        404,
        "Order not found",
    )
    val problem = ProblemDetails.forTypeAndDetail(type, "Order o-123 was not found.")

    check(problem.type == type.type)
    check(problem.status == 404)
    check(problem.title == "Order not found")
    check(problem.detail == "Order o-123 was not found.")
}

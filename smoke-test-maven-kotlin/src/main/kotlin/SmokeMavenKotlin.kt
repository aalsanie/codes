import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper

fun main() {
    check(HttpOutcomeMapper.standard().map(Outcome.of(StandardOutcomes.NOT_FOUND)).orNull()?.value == 404)
    println("Maven Kotlin consumer smoke test passed.")
}

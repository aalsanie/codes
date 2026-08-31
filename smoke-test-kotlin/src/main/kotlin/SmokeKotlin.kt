import io.github.aalsanie.codes.Issue
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.OutcomeCode
import io.github.aalsanie.codes.OutcomeDefinition
import io.github.aalsanie.codes.OutcomeRegistry
import io.github.aalsanie.codes.OutcomeState
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.ValidationResult
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper
import io.github.aalsanie.codes.protocol.http.HttpStatusCode

fun main() {
    val rejected = OutcomeDefinition.custom(
        "com.example.orders",
        "ORDER_REJECTED",
        OutcomeState.FAILED,
        "Order rejected.",
    )
    val outcome = Outcome.of(rejected, "internal=42", listOf(Issue.at("items[0]", "Unavailable.")))
    val detail: String? = outcome.detail
    val issueCode: OutcomeCode? = outcome.issues.first().code
    val mapper = HttpOutcomeMapper.standard().withMapping(rejected, HttpStatusCode.of(422))
    val mappedStatus: HttpStatusCode? = mapper.map(outcome).orNull()
    val missingDefinition: OutcomeDefinition? =
        OutcomeRegistry.empty().find("com.example:MISSING")
    check(mappedStatus?.value == 422)
    check(detail == "internal=42")
    check(issueCode == null)
    check(missingDefinition == null)
    check(outcome.code.value == "com.example.orders:ORDER_REJECTED")

    val validation = ValidationResult.combine(
        ValidationResult.valid(),
        ValidationResult.invalid(Issue.at("email", "Invalid.")),
    )
    check(validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT).issues.size == 1)
    check(OutcomeRegistry.standard().with(rejected).find(rejected.code) === rejected)

    println("Kotlin consumer smoke test passed.")
}

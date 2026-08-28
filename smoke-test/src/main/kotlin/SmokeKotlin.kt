import io.github.aalsanie.codes.Issue
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.OutcomeDefinition
import io.github.aalsanie.codes.OutcomeState
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.ValidationResult
import io.github.aalsanie.codes.protocol.grpc.GrpcOutcomeMapper
import io.github.aalsanie.codes.protocol.grpc.GrpcStatusCode
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper
import io.github.aalsanie.codes.protocol.http.HttpStatusCode

fun main() {
    check(HttpOutcomeMapper.standard().map(StandardOutcomes.NOT_FOUND).orNull() == HttpStatusCode.NOT_FOUND)
    check(GrpcOutcomeMapper.standard().map(StandardOutcomes.NOT_FOUND).orNull() == GrpcStatusCode.NOT_FOUND)

    val declined =
        OutcomeDefinition.custom(
            "com.example.payments",
            "PAYMENT_DECLINED",
            OutcomeState.FAILED,
            "The payment was declined.",
        )
    val mapper = HttpOutcomeMapper.standard().withMapping(declined, HttpStatusCode.of(422))
    check(mapper.map(Outcome.of(declined, "issuer response omitted")).orNull() == HttpStatusCode.of(422))

    val validation = ValidationResult.invalid(Issue.at("email", "The email address is invalid."))
    check(validation.toOutcome(StandardOutcomes.INVALID_ARGUMENT).definition === StandardOutcomes.INVALID_ARGUMENT)
}

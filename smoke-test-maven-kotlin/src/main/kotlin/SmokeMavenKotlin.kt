import com.google.rpc.ErrorInfo
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.grpc.GoogleRpcOutcomeMapper
import io.github.aalsanie.codes.protocol.http.HttpOutcomeMapper
import io.github.aalsanie.codes.spring.OutcomeProblemDetailMapper

fun main() {
    val outcome = Outcome.of(StandardOutcomes.NOT_FOUND)

    check(HttpOutcomeMapper.standard().map(outcome).orNull()?.value == 404)
    check(OutcomeProblemDetailMapper.safeDefaults().map(outcome).orNull()?.status == 404)

    val rpcStatus = GoogleRpcOutcomeMapper.safeDefaults().map(outcome).orNull()
    check(rpcStatus?.code == io.grpc.Status.Code.NOT_FOUND.value())

    val identity = rpcStatus!!.detailsList.first().unpack(ErrorInfo::class.java)
    check(identity.domain == outcome.code.namespace)
    check(identity.reason == outcome.code.name)

    println("Maven Kotlin consumer smoke test passed for all Codes artifacts.")
}

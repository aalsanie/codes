package io.github.aalsanie.codes.protocol.grpc

import io.github.aalsanie.codes.MappingResult
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.OutcomeCode
import io.github.aalsanie.codes.OutcomeDefinition
import io.github.aalsanie.codes.OutcomeMapper
import io.github.aalsanie.codes.StandardOutcomes
import io.github.aalsanie.codes.internal.InternalMappings

class GrpcOutcomeMapper private constructor(
    private val mappings: InternalMappings<GrpcStatusCode>,
) : OutcomeMapper<GrpcStatusCode> {
    val size: Int
        get() = mappings.size

    override fun map(definition: OutcomeDefinition): MappingResult<GrpcStatusCode> = mappings.map(definition.code)

    override fun map(outcome: Outcome): MappingResult<GrpcStatusCode> = mappings.map(outcome.code)

    fun contains(code: OutcomeCode): Boolean = mappings.contains(code)

    fun withMapping(
        definition: OutcomeDefinition,
        status: GrpcStatusCode,
    ): GrpcOutcomeMapper = GrpcOutcomeMapper(mappings.add(definition.code, status))

    fun withOverride(
        definition: OutcomeDefinition,
        status: GrpcStatusCode,
    ): GrpcOutcomeMapper = GrpcOutcomeMapper(mappings.override(definition.code, status))

    fun mappings(): Map<OutcomeCode, GrpcStatusCode> = mappings.entries()

    companion object {
        @JvmStatic
        fun empty(): GrpcOutcomeMapper = GrpcOutcomeMapper(InternalMappings.empty())

        @JvmStatic
        fun standard(): GrpcOutcomeMapper =
            GrpcOutcomeMapper(
                InternalMappings.from(
                    linkedMapOf(
                        StandardOutcomes.OK.code to GrpcStatusCode.OK,
                        StandardOutcomes.CREATED.code to GrpcStatusCode.OK,
                        StandardOutcomes.ACCEPTED.code to GrpcStatusCode.OK,
                        StandardOutcomes.NO_CONTENT.code to GrpcStatusCode.OK,
                        StandardOutcomes.INVALID_ARGUMENT.code to GrpcStatusCode.INVALID_ARGUMENT,
                        StandardOutcomes.UNAUTHENTICATED.code to GrpcStatusCode.UNAUTHENTICATED,
                        StandardOutcomes.PERMISSION_DENIED.code to GrpcStatusCode.PERMISSION_DENIED,
                        StandardOutcomes.NOT_FOUND.code to GrpcStatusCode.NOT_FOUND,
                        StandardOutcomes.ALREADY_EXISTS.code to GrpcStatusCode.ALREADY_EXISTS,
                        StandardOutcomes.FAILED_PRECONDITION.code to GrpcStatusCode.FAILED_PRECONDITION,
                        StandardOutcomes.OUT_OF_RANGE.code to GrpcStatusCode.OUT_OF_RANGE,
                        StandardOutcomes.PAYLOAD_TOO_LARGE.code to GrpcStatusCode.RESOURCE_EXHAUSTED,
                        StandardOutcomes.RATE_LIMITED.code to GrpcStatusCode.RESOURCE_EXHAUSTED,
                        StandardOutcomes.CANCELLED.code to GrpcStatusCode.CANCELLED,
                        StandardOutcomes.DEADLINE_EXCEEDED.code to GrpcStatusCode.DEADLINE_EXCEEDED,
                        StandardOutcomes.ABORTED.code to GrpcStatusCode.ABORTED,
                        StandardOutcomes.UNIMPLEMENTED.code to GrpcStatusCode.UNIMPLEMENTED,
                        StandardOutcomes.UNAVAILABLE.code to GrpcStatusCode.UNAVAILABLE,
                        StandardOutcomes.INTERNAL.code to GrpcStatusCode.INTERNAL,
                        StandardOutcomes.DATA_LOSS.code to GrpcStatusCode.DATA_LOSS,
                        StandardOutcomes.RESOURCE_EXHAUSTED.code to GrpcStatusCode.RESOURCE_EXHAUSTED,
                    ),
                ),
            )
    }
}

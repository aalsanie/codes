package io.github.aalsanie.codes.protocol.http

import io.github.aalsanie.codes.InternalMappings
import io.github.aalsanie.codes.MappingResult
import io.github.aalsanie.codes.Outcome
import io.github.aalsanie.codes.OutcomeCode
import io.github.aalsanie.codes.OutcomeDefinition
import io.github.aalsanie.codes.OutcomeMapper
import io.github.aalsanie.codes.StandardOutcomes

class HttpOutcomeMapper private constructor(
    private val mappings: InternalMappings<HttpStatusCode>,
) : OutcomeMapper<HttpStatusCode> {
    val size: Int
        get() = mappings.size

    override fun map(definition: OutcomeDefinition): MappingResult<HttpStatusCode> = mappings.map(definition.code)

    fun map(outcome: Outcome): MappingResult<HttpStatusCode> = mappings.map(outcome.code)

    fun contains(code: OutcomeCode): Boolean = mappings.contains(code)

    fun withMapping(definition: OutcomeDefinition, status: HttpStatusCode): HttpOutcomeMapper =
        HttpOutcomeMapper(mappings.add(definition.code, status))

    fun withOverride(definition: OutcomeDefinition, status: HttpStatusCode): HttpOutcomeMapper =
        HttpOutcomeMapper(mappings.override(definition.code, status))

    fun mappings(): Map<OutcomeCode, HttpStatusCode> = mappings.entries()

    companion object {
        @JvmStatic
        fun empty(): HttpOutcomeMapper = HttpOutcomeMapper(InternalMappings.empty())

        @JvmStatic
        fun standard(): HttpOutcomeMapper =
            HttpOutcomeMapper(
                InternalMappings.from(
                    linkedMapOf(
                        StandardOutcomes.OK.code to HttpStatusCode.OK,
                        StandardOutcomes.CREATED.code to HttpStatusCode.CREATED,
                        StandardOutcomes.ACCEPTED.code to HttpStatusCode.ACCEPTED,
                        StandardOutcomes.NO_CONTENT.code to HttpStatusCode.NO_CONTENT,
                        StandardOutcomes.INVALID_ARGUMENT.code to HttpStatusCode.BAD_REQUEST,
                        StandardOutcomes.UNAUTHENTICATED.code to HttpStatusCode.UNAUTHORIZED,
                        StandardOutcomes.PERMISSION_DENIED.code to HttpStatusCode.FORBIDDEN,
                        StandardOutcomes.NOT_FOUND.code to HttpStatusCode.NOT_FOUND,
                        StandardOutcomes.ALREADY_EXISTS.code to HttpStatusCode.CONFLICT,
                        StandardOutcomes.OUT_OF_RANGE.code to HttpStatusCode.BAD_REQUEST,
                        StandardOutcomes.PAYLOAD_TOO_LARGE.code to HttpStatusCode.PAYLOAD_TOO_LARGE,
                        StandardOutcomes.RATE_LIMITED.code to HttpStatusCode.TOO_MANY_REQUESTS,
                        StandardOutcomes.UNIMPLEMENTED.code to HttpStatusCode.NOT_IMPLEMENTED,
                        StandardOutcomes.UNAVAILABLE.code to HttpStatusCode.SERVICE_UNAVAILABLE,
                        StandardOutcomes.INTERNAL.code to HttpStatusCode.INTERNAL_SERVER_ERROR,
                        StandardOutcomes.DATA_LOSS.code to HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ),
                ),
            )
    }
}

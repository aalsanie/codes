package io.github.aalsanie.codes

import java.util.Collections

object StandardOutcomes {
    const val NAMESPACE = "io.github.aalsanie.codes.standard"

    @JvmField val OK = OutcomeDefinition.standard("OK")

    @JvmField val INVALID_ARGUMENT = OutcomeDefinition.standard("INVALID_ARGUMENT")

    @JvmField val UNAUTHENTICATED = OutcomeDefinition.standard("UNAUTHENTICATED")

    @JvmField val PERMISSION_DENIED = OutcomeDefinition.standard("PERMISSION_DENIED")

    @JvmField val NOT_FOUND = OutcomeDefinition.standard("NOT_FOUND")

    @JvmField val ALREADY_EXISTS = OutcomeDefinition.standard("ALREADY_EXISTS")

    @JvmField val FAILED_PRECONDITION = OutcomeDefinition.standard("FAILED_PRECONDITION")

    @JvmField val OUT_OF_RANGE = OutcomeDefinition.standard("OUT_OF_RANGE")

    @JvmField val RATE_LIMITED = OutcomeDefinition.standard("RATE_LIMITED")

    @JvmField val CANCELLED = OutcomeDefinition.standard("CANCELLED")

    @JvmField val DEADLINE_EXCEEDED = OutcomeDefinition.standard("DEADLINE_EXCEEDED")

    @JvmField val ABORTED = OutcomeDefinition.standard("ABORTED")

    @JvmField val UNIMPLEMENTED = OutcomeDefinition.standard("UNIMPLEMENTED")

    @JvmField val UNAVAILABLE = OutcomeDefinition.standard("UNAVAILABLE")

    @JvmField val INTERNAL = OutcomeDefinition.standard("INTERNAL")

    @JvmField val DATA_LOSS = OutcomeDefinition.standard("DATA_LOSS")

    @JvmField val RESOURCE_EXHAUSTED = OutcomeDefinition.standard("RESOURCE_EXHAUSTED")

    @JvmField
    val all: List<OutcomeDefinition> =
        Collections.unmodifiableList(
            listOf(
                OK,
                INVALID_ARGUMENT,
                UNAUTHENTICATED,
                PERMISSION_DENIED,
                NOT_FOUND,
                ALREADY_EXISTS,
                FAILED_PRECONDITION,
                OUT_OF_RANGE,
                RATE_LIMITED,
                CANCELLED,
                DEADLINE_EXCEEDED,
                ABORTED,
                UNIMPLEMENTED,
                UNAVAILABLE,
                INTERNAL,
                DATA_LOSS,
                RESOURCE_EXHAUSTED,
            ),
        )
}

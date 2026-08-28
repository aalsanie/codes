package io.github.aalsanie.codes

import io.github.aalsanie.codes.internal.Constraints

class OutcomeDefinition private constructor(
    val code: OutcomeCode,
    val state: OutcomeState,
    val defaultMessage: String,
) {
    override fun toString(): String = "OutcomeDefinition(code=$code, state=$state, defaultMessage=$defaultMessage)"

    companion object {
        private const val ROOT_NAMESPACE = "io.github.aalsanie.codes"
        private const val STANDARD_NAMESPACE = "io.github.aalsanie.codes.standard"

        private val standardDefinitions: Map<String, OutcomeDefinition> by lazy(LazyThreadSafetyMode.PUBLICATION) {
            linkedMapOf(
                "OK" to standardDefinition("OK", OutcomeState.SUCCEEDED, "The operation completed successfully."),
                "INVALID_ARGUMENT" to
                    standardDefinition(
                        "INVALID_ARGUMENT",
                        OutcomeState.FAILED,
                        "An argument supplied to the operation is invalid.",
                    ),
                "UNAUTHENTICATED" to
                    standardDefinition(
                        "UNAUTHENTICATED",
                        OutcomeState.FAILED,
                        "Authentication is required or invalid.",
                    ),
                "PERMISSION_DENIED" to
                    standardDefinition(
                        "PERMISSION_DENIED",
                        OutcomeState.FAILED,
                        "The authenticated caller does not have permission.",
                    ),
                "NOT_FOUND" to standardDefinition("NOT_FOUND", OutcomeState.FAILED, "The requested resource was not found."),
                "ALREADY_EXISTS" to
                    standardDefinition(
                        "ALREADY_EXISTS",
                        OutcomeState.FAILED,
                        "The resource already exists.",
                    ),
                "FAILED_PRECONDITION" to
                    standardDefinition(
                        "FAILED_PRECONDITION",
                        OutcomeState.FAILED,
                        "A required precondition was not satisfied.",
                    ),
                "OUT_OF_RANGE" to
                    standardDefinition(
                        "OUT_OF_RANGE",
                        OutcomeState.FAILED,
                        "A value is outside the allowed range.",
                    ),
                "RATE_LIMITED" to
                    standardDefinition(
                        "RATE_LIMITED",
                        OutcomeState.FAILED,
                        "The caller exceeded an allowed operation rate.",
                    ),
                "CANCELLED" to
                    standardDefinition(
                        "CANCELLED",
                        OutcomeState.FAILED,
                        "The operation was cancelled before completion.",
                    ),
                "DEADLINE_EXCEEDED" to
                    standardDefinition(
                        "DEADLINE_EXCEEDED",
                        OutcomeState.FAILED,
                        "The operation exceeded its deadline.",
                    ),
                "ABORTED" to
                    standardDefinition(
                        "ABORTED",
                        OutcomeState.FAILED,
                        "The operation was aborted before completion.",
                    ),
                "UNIMPLEMENTED" to
                    standardDefinition(
                        "UNIMPLEMENTED",
                        OutcomeState.FAILED,
                        "The requested operation is not implemented.",
                    ),
                "UNAVAILABLE" to
                    standardDefinition(
                        "UNAVAILABLE",
                        OutcomeState.FAILED,
                        "The service is temporarily unavailable.",
                    ),
                "INTERNAL" to
                    standardDefinition(
                        "INTERNAL",
                        OutcomeState.FAILED,
                        "The service encountered an internal error.",
                    ),
                "DATA_LOSS" to
                    standardDefinition(
                        "DATA_LOSS",
                        OutcomeState.FAILED,
                        "Unrecoverable data loss or corruption was detected.",
                    ),
                "RESOURCE_EXHAUSTED" to
                    standardDefinition(
                        "RESOURCE_EXHAUSTED",
                        OutcomeState.FAILED,
                        "A required resource limit was exhausted.",
                    ),
            )
        }

        @JvmStatic
        fun custom(
            namespace: String,
            name: String,
            state: OutcomeState,
            defaultMessage: String,
        ): OutcomeDefinition = custom(OutcomeCode.of(namespace, name), state, defaultMessage)

        @JvmStatic
        fun custom(
            code: OutcomeCode,
            state: OutcomeState,
            defaultMessage: String,
        ): OutcomeDefinition {
            require(!isReservedNamespace(code.namespace)) {
                "namespace '${code.namespace}' is reserved"
            }
            Constraints.requireMessage(defaultMessage)
            return OutcomeDefinition(code, state, defaultMessage)
        }

        @JvmSynthetic
        internal fun standard(name: String): OutcomeDefinition = standardDefinitions[name] ?: error("unknown standard outcome: $name")

        private fun standardDefinition(
            name: String,
            state: OutcomeState,
            defaultMessage: String,
        ): OutcomeDefinition {
            Constraints.requireMessage(defaultMessage)
            return OutcomeDefinition(
                code = OutcomeCode.of(STANDARD_NAMESPACE, name),
                state = state,
                defaultMessage = defaultMessage,
            )
        }

        private fun isReservedNamespace(namespace: String): Boolean = namespace == ROOT_NAMESPACE || namespace.startsWith("$ROOT_NAMESPACE.")
    }
}

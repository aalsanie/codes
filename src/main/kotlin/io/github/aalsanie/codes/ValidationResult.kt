package io.github.aalsanie.codes

import java.util.Collections

sealed class ValidationResult private constructor() {
    data object Valid : ValidationResult()

    class Invalid private constructor(
        issues: List<Issue>,
    ) : ValidationResult() {
        val issues: List<Issue> = Collections.unmodifiableList(ArrayList(issues))

        init {
            require(this.issues.isNotEmpty()) {
                "invalid validation result requires at least one issue"
            }
        }

        override fun equals(other: Any?): Boolean =
            this === other ||
                (other is Invalid && issues == other.issues)

        override fun hashCode(): Int = issues.hashCode()

        override fun toString(): String = "Invalid(issues=$issues)"

        internal companion object {
            @JvmSynthetic
            internal fun create(issues: List<Issue>): Invalid = Invalid(issues)
        }
    }

    val isValid: Boolean
        get() = this === Valid

    val isInvalid: Boolean
        get() = this is Invalid

    fun issues(): List<Issue> =
        when (this) {
            Valid -> emptyList()
            is Invalid -> issues
        }

    @JvmOverloads
    fun toOutcome(
        failureDefinition: OutcomeDefinition,
        detail: String? = null,
    ): Outcome {
        require(failureDefinition.state == OutcomeState.FAILED) {
            "validation failure definition must have FAILED state"
        }
        return when (this) {
            Valid -> Outcome.of(StandardOutcomes.OK, detail)
            is Invalid -> Outcome.of(failureDefinition, detail, issues)
        }
    }

    companion object {
        @JvmStatic
        fun valid(): ValidationResult = Valid

        @JvmStatic
        fun invalid(issue: Issue): ValidationResult = Invalid.create(listOf(issue))

        @JvmStatic
        fun invalid(issues: List<Issue>): ValidationResult = Invalid.create(issues)

        @JvmStatic
        fun combine(vararg results: ValidationResult): ValidationResult = combine(results.asList())

        @JvmStatic
        fun combine(results: List<ValidationResult>): ValidationResult {
            val issues =
                results.flatMap { result ->
                    when (result) {
                        Valid -> emptyList()
                        is Invalid -> result.issues
                    }
                }
            return if (issues.isEmpty()) Valid else Invalid.create(issues)
        }
    }
}

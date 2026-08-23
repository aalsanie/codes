package io.github.aalsanie.codes

import java.util.Collections

class Outcome private constructor(
    val definition: OutcomeDefinition,
    val detail: String?,
    issues: List<Issue>,
) {
    val code: OutcomeCode
        get() = definition.code

    val state: OutcomeState
        get() = definition.state

    val defaultMessage: String
        get() = definition.defaultMessage

    val message: String
        get() = defaultMessage

    val issues: List<Issue> = Collections.unmodifiableList(ArrayList(issues))

    val isSuccessful: Boolean
        get() = state == OutcomeState.SUCCEEDED

    val isPending: Boolean
        get() = state == OutcomeState.PENDING

    val isFailed: Boolean
        get() = state == OutcomeState.FAILED

    val isTerminal: Boolean
        get() = state != OutcomeState.PENDING

    override fun toString(): String =
        "Outcome(code=$code, state=$state, message=$message, issues=${issues.size})"

    companion object {
        @JvmStatic
        @JvmOverloads
        fun of(
            definition: OutcomeDefinition,
            detail: String? = null,
            issues: List<Issue> = emptyList(),
        ): Outcome {
            if (detail != null) {
                Constraints.requireDetail(detail)
            }
            require(definition.state == OutcomeState.FAILED || issues.isEmpty()) {
                "issues are only allowed on failed outcomes"
            }
            return Outcome(definition, detail, issues)
        }
    }
}

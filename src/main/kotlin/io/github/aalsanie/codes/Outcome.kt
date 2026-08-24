package io.github.aalsanie.codes

import io.github.aalsanie.codes.internal.Constraints
import java.util.Collections

public class Outcome private constructor(
    public val definition: OutcomeDefinition,
    public val detail: String?,
    issues: List<Issue>,
) {
    public val code: OutcomeCode
        get() = definition.code

    public val state: OutcomeState
        get() = definition.state

    public val defaultMessage: String
        get() = definition.defaultMessage

    public val message: String
        get() = defaultMessage

    public val issues: List<Issue> = Collections.unmodifiableList(ArrayList(issues))

    public val isSuccessful: Boolean
        get() = state == OutcomeState.SUCCEEDED

    public val isPending: Boolean
        get() = state == OutcomeState.PENDING

    public val isFailed: Boolean
        get() = state == OutcomeState.FAILED

    public val isTerminal: Boolean
        get() = state != OutcomeState.PENDING

    public override fun toString(): String = "Outcome(code=$code, state=$state, message=$message, issues=${issues.size})"

    public companion object {
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

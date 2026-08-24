package io.github.aalsanie.codes

import io.github.aalsanie.codes.internal.Constraints

public class Issue private constructor(
    public val code: OutcomeCode?,
    public val path: String?,
    public val message: String,
) {
    public override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is Issue &&
                    code == other.code &&
                    path == other.path &&
                    message == other.message
            )

    public override fun hashCode(): Int {
        var result = code?.hashCode() ?: 0
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        return result
    }

    public override fun toString(): String =
        buildString {
            if (code != null) {
                append(code)
                append(' ')
            }
            if (path != null) {
                append(path)
                append(": ")
            }
            append(message)
        }

    public companion object {
        @JvmStatic
        public fun of(message: String): Issue = create(null, null, message)

        @JvmStatic
        public fun coded(
            code: OutcomeCode,
            message: String,
        ): Issue = create(code, null, message)

        @JvmStatic
        public fun at(
            path: String,
            message: String,
        ): Issue = create(null, path, message)

        @JvmStatic
        public fun at(
            path: String,
            code: OutcomeCode,
            message: String,
        ): Issue = create(code, path, message)

        private fun create(
            code: OutcomeCode?,
            path: String?,
            message: String,
        ): Issue {
            if (path != null) {
                Constraints.requirePath(path)
            }
            Constraints.requireMessage(message)
            return Issue(code, path, message)
        }
    }
}

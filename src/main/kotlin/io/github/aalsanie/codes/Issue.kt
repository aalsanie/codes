package io.github.aalsanie.codes

class Issue private constructor(
    val code: OutcomeCode?,
    val path: String?,
    val message: String,
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            other is Issue &&
            code == other.code &&
            path == other.path &&
            message == other.message

    override fun hashCode(): Int {
        var result = code?.hashCode() ?: 0
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String =
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

    companion object {
        @JvmStatic
        fun of(message: String): Issue = create(null, null, message)

        @JvmStatic
        fun coded(code: OutcomeCode, message: String): Issue = create(code, null, message)

        @JvmStatic
        fun at(path: String, message: String): Issue = create(null, path, message)

        @JvmStatic
        fun at(path: String, code: OutcomeCode, message: String): Issue = create(code, path, message)

        private fun create(code: OutcomeCode?, path: String?, message: String): Issue {
            if (path != null) {
                Constraints.requirePath(path)
            }
            Constraints.requireMessage(message)
            return Issue(code, path, message)
        }
    }
}

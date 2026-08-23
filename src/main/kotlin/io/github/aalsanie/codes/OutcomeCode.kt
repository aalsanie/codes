package io.github.aalsanie.codes

class OutcomeCode private constructor(
    val namespace: String,
    val name: String,
) : Comparable<OutcomeCode> {
    val value: String = "$namespace:$name"

    override fun compareTo(other: OutcomeCode): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is OutcomeCode &&
            namespace == other.namespace &&
            name == other.name

    override fun hashCode(): Int = 31 * namespace.hashCode() + name.hashCode()

    override fun toString(): String = value

    companion object {
        private const val DELIMITER = ':'

        @JvmStatic
        fun of(namespace: String, name: String): OutcomeCode {
            Constraints.requireNamespace(namespace)
            Constraints.requireName(name)
            return OutcomeCode(namespace, name)
        }

        @JvmStatic
        fun parse(value: String): OutcomeCode {
            val delimiterIndex = value.indexOf(DELIMITER)
            require(delimiterIndex > 0 && delimiterIndex == value.lastIndexOf(DELIMITER)) {
                "outcome code must use the format namespace:NAME"
            }
            return of(
                namespace = value.substring(0, delimiterIndex),
                name = value.substring(delimiterIndex + 1),
            )
        }

        @JvmStatic
        fun parseOrNull(value: String): OutcomeCode? =
            try {
                parse(value)
            } catch (_: IllegalArgumentException) {
                null
            }
    }
}

package io.github.aalsanie.codes

sealed class MappingResult<out T : Any> private constructor() {
    class Mapped<T : Any> internal constructor(val value: T) : MappingResult<T>() {
        override fun equals(other: Any?): Boolean =
            this === other || other is Mapped<*> && value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "Mapped(value=$value)"
    }

    data object Unmapped : MappingResult<Nothing>()

    val isMapped: Boolean
        get() = this is Mapped<*>

    val isUnmapped: Boolean
        get() = this === Unmapped

    fun orNull(): T? =
        when (this) {
            is Mapped -> value
            Unmapped -> null
        }

    inline fun <R> fold(onMapped: (T) -> R, onUnmapped: () -> R): R =
        when (this) {
            is Mapped -> onMapped(value)
            Unmapped -> onUnmapped()
        }

    companion object {
        @JvmStatic
        fun <T : Any> mapped(value: T): MappingResult<T> = Mapped(value)

        @JvmStatic
        fun <T : Any> unmapped(): MappingResult<T> = Unmapped
    }
}

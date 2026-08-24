package io.github.aalsanie.codes.internal

import io.github.aalsanie.codes.MappingResult
import io.github.aalsanie.codes.OutcomeCode
import java.util.Collections

internal class InternalMappings<T : Any> private constructor(
    mappings: Map<OutcomeCode, T>,
) {
    private val values: Map<OutcomeCode, T> =
        Collections.unmodifiableMap(LinkedHashMap(mappings))

    @get:JvmSynthetic
    internal val size: Int
        get() = values.size

    @JvmSynthetic
    internal fun contains(code: OutcomeCode): Boolean = values.containsKey(code)

    @JvmSynthetic
    internal fun map(code: OutcomeCode): MappingResult<T> =
        values[code]?.let { MappingResult.mapped(it) }
            ?: MappingResult.unmapped()

    @JvmSynthetic
    internal fun add(
        code: OutcomeCode,
        value: T,
    ): InternalMappings<T> {
        require(code !in values) {
            "mapping already exists for outcome code: $code"
        }

        val next = LinkedHashMap(values)
        next[code] = value

        return InternalMappings(next)
    }

    @JvmSynthetic
    internal fun override(
        code: OutcomeCode,
        value: T,
    ): InternalMappings<T> {
        require(code in values) {
            "cannot override missing mapping for outcome code: $code"
        }

        val next = LinkedHashMap(values)
        next[code] = value

        return InternalMappings(next)
    }

    @JvmSynthetic
    internal fun entries(): Map<OutcomeCode, T> = Collections.unmodifiableMap(LinkedHashMap(values))

    internal companion object {
        @JvmSynthetic
        internal fun <T : Any> empty(): InternalMappings<T> = InternalMappings(emptyMap())

        @JvmSynthetic
        internal fun <T : Any> from(
            entries: Map<OutcomeCode, T>,
        ): InternalMappings<T> = InternalMappings(entries)
    }
}

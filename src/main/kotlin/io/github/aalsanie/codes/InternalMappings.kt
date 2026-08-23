package io.github.aalsanie.codes

import java.util.Collections

internal class InternalMappings<T : Any> private constructor(
    mappings: Map<OutcomeCode, T>,
) {
    private val values: Map<OutcomeCode, T> =
        Collections.unmodifiableMap(LinkedHashMap(mappings))

    val size: Int
        get() = values.size

    fun contains(code: OutcomeCode): Boolean = values.containsKey(code)

    fun map(code: OutcomeCode): MappingResult<T> =
        values[code]?.let { MappingResult.mapped(it) } ?: MappingResult.unmapped()

    fun add(code: OutcomeCode, value: T): InternalMappings<T> {
        require(code !in values) {
            "mapping already exists for outcome code: $code"
        }
        val next = LinkedHashMap(values)
        next[code] = value
        return InternalMappings(next)
    }

    fun override(code: OutcomeCode, value: T): InternalMappings<T> {
        require(code in values) {
            "cannot override missing mapping for outcome code: $code"
        }
        val next = LinkedHashMap(values)
        next[code] = value
        return InternalMappings(next)
    }

    fun entries(): Map<OutcomeCode, T> =
        Collections.unmodifiableMap(LinkedHashMap(values))

    companion object {
        fun <T : Any> empty(): InternalMappings<T> = InternalMappings(emptyMap())

        fun <T : Any> from(entries: Map<OutcomeCode, T>): InternalMappings<T> = InternalMappings(entries)
    }
}

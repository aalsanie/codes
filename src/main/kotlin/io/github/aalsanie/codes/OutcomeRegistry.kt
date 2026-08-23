package io.github.aalsanie.codes

import java.util.Collections

class OutcomeRegistry private constructor(
    definitions: Map<OutcomeCode, OutcomeDefinition>,
) {
    private val definitionsByCode: Map<OutcomeCode, OutcomeDefinition> =
        Collections.unmodifiableMap(LinkedHashMap(definitions))

    val size: Int
        get() = definitionsByCode.size

    fun contains(code: OutcomeCode): Boolean = definitionsByCode.containsKey(code)

    fun find(code: OutcomeCode): OutcomeDefinition? = definitionsByCode[code]

    fun find(value: String): OutcomeDefinition? = OutcomeCode.parseOrNull(value)?.let(::find)

    fun require(code: OutcomeCode): OutcomeDefinition =
        definitionsByCode[code] ?: throw NoSuchElementException("unknown outcome code: $code")

    fun definitions(): List<OutcomeDefinition> =
        Collections.unmodifiableList(ArrayList(definitionsByCode.values))

    fun with(definition: OutcomeDefinition): OutcomeRegistry {
        require(definition.code !in definitionsByCode) {
            "outcome code already registered: ${definition.code}"
        }
        val next = LinkedHashMap(definitionsByCode)
        next[definition.code] = definition
        return OutcomeRegistry(next)
    }

    fun withAll(definitions: Iterable<OutcomeDefinition>): OutcomeRegistry {
        var next = this
        definitions.forEach { definition ->
            next = next.with(definition)
        }
        return next
    }

    companion object {
        @JvmStatic
        fun empty(): OutcomeRegistry = OutcomeRegistry(emptyMap())

        @JvmStatic
        fun of(vararg definitions: OutcomeDefinition): OutcomeRegistry =
            empty().withAll(definitions.asList())

        @JvmStatic
        fun standard(): OutcomeRegistry = of(*StandardOutcomes.all.toTypedArray())
    }
}

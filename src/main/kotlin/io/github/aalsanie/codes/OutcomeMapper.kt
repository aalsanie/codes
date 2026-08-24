package io.github.aalsanie.codes

fun interface OutcomeMapper<T : Any> {
    fun map(definition: OutcomeDefinition): MappingResult<T>

    fun map(outcome: Outcome): MappingResult<T> = map(outcome.definition)
}

package io.github.aalsanie.codes

fun interface OutcomeMapper<T : Any> {
    fun map(definition: OutcomeDefinition): MappingResult<T>
}

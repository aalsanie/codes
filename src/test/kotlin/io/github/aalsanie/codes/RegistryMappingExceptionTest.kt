package io.github.aalsanie.codes

import org.junit.jupiter.api.Test

class RegistryMappingExceptionTest {
    private val custom = OutcomeDefinition.custom("com.example", "CUSTOM", OutcomeState.FAILED, "Custom failure.")

    @Test fun emptyRegistryIsEmpty() { assertEquals(0, OutcomeRegistry.empty().size) }
    @Test fun standardRegistryContainsAllStandards() { assertEquals(StandardOutcomes.all.size, OutcomeRegistry.standard().size) }

    @Test fun registryFindsByCodeAndString() {
        val registry = OutcomeRegistry.of(custom)
        assertSame(custom, registry.find(custom.code))
        assertSame(custom, registry.find(custom.code.value))
        assertNull(registry.find("invalid"))
    }

    @Test fun registryContainsCode() { assertTrue(OutcomeRegistry.of(custom).contains(custom.code)) }

    @Test fun registryRequireReturnsDefinition() { assertSame(custom, OutcomeRegistry.of(custom).require(custom.code)) }

    @Test fun registryRequireRejectsMissingCode() {
        assertFails<NoSuchElementException> { OutcomeRegistry.empty().require(custom.code) }
    }

    @Test fun registryRejectsDuplicateCode() {
        val duplicate = OutcomeDefinition.custom(custom.code, OutcomeState.FAILED, "Other message.")
        assertFails<IllegalArgumentException> { OutcomeRegistry.of(custom, duplicate) }
    }

    @Test fun registryWithIsPersistent() {
        val empty = OutcomeRegistry.empty()
        val next = empty.with(custom)
        assertEquals(0, empty.size)
        assertEquals(1, next.size)
    }

    @Test fun registryWithAllPreservesOrder() {
        val second = OutcomeDefinition.custom("com.example", "SECOND", OutcomeState.SUCCEEDED, "Second.")
        val registry = OutcomeRegistry.empty().withAll(listOf(custom, second))
        assertEquals(listOf(custom, second), registry.definitions())
    }

    @Test fun registryDefinitionsAreUnmodifiable() {
        @Suppress("UNCHECKED_CAST")
        val values = OutcomeRegistry.of(custom).definitions() as MutableList<OutcomeDefinition>
        assertFails<UnsupportedOperationException> { values.clear() }
    }

    @Test fun mappedResultExposesValue() {
        val mapped = MappingResult.mapped("x")
        assertTrue(mapped.isMapped)
        assertFalse(mapped.isUnmapped)
        assertEquals("x", mapped.orNull())
        assertEquals("mapped:x", mapped.fold({ "mapped:$it" }, { "none" }))
    }

    @Test fun unmappedResultExposesAbsence() {
        val unmapped = MappingResult.unmapped<String>()
        assertFalse(unmapped.isMapped)
        assertTrue(unmapped.isUnmapped)
        assertNull(unmapped.orNull())
        assertEquals("none", unmapped.fold({ "mapped:$it" }, { "none" }))
    }

    @Test fun mappedResultHasValueEquality() {
        assertEquals(MappingResult.mapped("x"), MappingResult.mapped("x"))
        assertEquals(MappingResult.mapped("x").hashCode(), MappingResult.mapped("x").hashCode())
        assertNotEquals(MappingResult.mapped("x"), MappingResult.mapped("y"))
        assertTrue(MappingResult.mapped("x").toString().contains("x"))
    }

    @Test fun exceptionRequiresFailedOutcome() {
        assertFails<IllegalArgumentException> { OutcomeException(Outcome.of(StandardOutcomes.OK)) }
    }

    @Test fun exceptionUsesStableMessageAndCause() {
        val cause = IllegalStateException("root")
        val outcome = Outcome.of(StandardOutcomes.INTERNAL, "sensitive internal detail")
        val exception = outcome.toException(cause)
        assertSame(outcome, exception.outcome)
        assertSame(cause, exception.cause)
        assertEquals(StandardOutcomes.INTERNAL.defaultMessage, exception.message)
        assertFalse(exception.message!!.contains("sensitive"))
    }
}

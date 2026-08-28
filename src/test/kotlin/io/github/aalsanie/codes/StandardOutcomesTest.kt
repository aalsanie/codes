package io.github.aalsanie.codes

import org.junit.jupiter.api.Test

class StandardOutcomesTest {
    @Test fun containsUniqueDefinitions() {
        assertEquals(
            StandardOutcomes.all.size,
            StandardOutcomes.all
                .map { it.code }
                .toSet()
                .size,
        )
    }

    @Test fun exposesExpectedCount() {
        assertEquals(17, StandardOutcomes.all.size)
    }

    @Test fun allDefinitionsUseReservedNamespace() {
        assertTrue(StandardOutcomes.all.all { it.code.namespace == StandardOutcomes.NAMESPACE })
    }

    @Test fun okIsTheOnlyStandardSuccess() {
        assertEquals(OutcomeState.SUCCEEDED, StandardOutcomes.OK.state)
        assertEquals(listOf(StandardOutcomes.OK), StandardOutcomes.all.filter { it.state == OutcomeState.SUCCEEDED })
    }

    @Test fun standardCatalogContainsNoPendingOutcome() {
        assertTrue(StandardOutcomes.all.none { it.state == OutcomeState.PENDING })
    }

    @Test fun allNonOkStandardsAreFailures() {
        assertTrue(StandardOutcomes.all.filterNot { it === StandardOutcomes.OK }.all { it.state == OutcomeState.FAILED })
    }

    @Test fun allListIsNotMutableFromJavaView() {
        @Suppress("UNCHECKED_CAST")
        val mutable = StandardOutcomes.all as MutableList<OutcomeDefinition>
        assertFails<UnsupportedOperationException> { mutable.add(StandardOutcomes.OK) }
    }
}

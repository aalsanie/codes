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
        assertEquals(21, StandardOutcomes.all.size)
    }

    @Test fun allDefinitionsUseReservedNamespace() {
        assertTrue(StandardOutcomes.all.all { it.code.namespace == StandardOutcomes.NAMESPACE })
    }

    @Test fun successDefinitionsHaveExpectedStates() {
        assertEquals(OutcomeState.SUCCEEDED, StandardOutcomes.OK.state)
        assertEquals(OutcomeState.SUCCEEDED, StandardOutcomes.CREATED.state)
        assertEquals(OutcomeState.SUCCEEDED, StandardOutcomes.NO_CONTENT.state)
    }

    @Test fun acceptedIsPending() {
        assertEquals(OutcomeState.PENDING, StandardOutcomes.ACCEPTED.state)
    }

    @Test fun cancellationIsFailure() {
        assertEquals(OutcomeState.FAILED, StandardOutcomes.CANCELLED.state)
    }

    @Test fun failureDefinitionsAreFailures() {
        val nonFailures = setOf(StandardOutcomes.OK, StandardOutcomes.CREATED, StandardOutcomes.ACCEPTED, StandardOutcomes.NO_CONTENT)
        assertTrue(StandardOutcomes.all.filterNot { it in nonFailures }.all { it.state == OutcomeState.FAILED })
    }

    @Test fun allListIsNotMutableFromJavaView() {
        @Suppress("UNCHECKED_CAST")
        val mutable = StandardOutcomes.all as MutableList<OutcomeDefinition>
        assertFails<UnsupportedOperationException> { mutable.add(StandardOutcomes.OK) }
    }
}

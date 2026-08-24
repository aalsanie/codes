package io.github.aalsanie.codes

import org.junit.jupiter.api.Test

class ValidationResultTest {
    @Test fun validHasNoIssues() {
        val result = ValidationResult.valid()
        assertTrue(result.isValid)
        assertFalse(result.isInvalid)
        assertTrue(result.issues().isEmpty())
    }

    @Test fun invalidContainsIssue() {
        val issue = Issue.of("Bad.")
        val result = ValidationResult.invalid(issue)
        assertFalse(result.isValid)
        assertTrue(result.isInvalid)
        assertEquals(listOf(issue), result.issues())
    }

    @Test fun invalidRejectsEmptyIssues() {
        assertFails<IllegalArgumentException> { ValidationResult.invalid(emptyList()) }
    }

    @Test fun invalidDefensivelyCopiesIssues() {
        val source = mutableListOf(Issue.of("Bad."))
        val result = ValidationResult.invalid(source)
        source.clear()
        assertEquals(1, result.issues().size)
    }

    @Test fun combinesValidResults() {
        assertSame(ValidationResult.Valid, ValidationResult.combine(ValidationResult.valid(), ValidationResult.valid()))
    }

    @Test fun combinesAndPreservesValidationIssues() {
        val first = Issue.at("a", "Bad a.")
        val second = Issue.at("b", "Bad b.")
        val result =
            ValidationResult.combine(
                ValidationResult.invalid(first),
                ValidationResult.valid(),
                ValidationResult.invalid(second),
            )
        assertEquals(listOf(first, second), result.issues())
    }

    @Test fun combinesListOverload() {
        val issue = Issue.of("Bad.")
        assertEquals(listOf(issue), ValidationResult.combine(listOf(ValidationResult.invalid(issue))).issues())
    }

    @Test fun validConvertsToSuccessOutcome() {
        val outcome = ValidationResult.valid().toOutcome()
        assertSame(StandardOutcomes.OK, outcome.definition)
    }

    @Test fun invalidConvertsOnlyToInvalidArgument() {
        val issue = Issue.of("Bad.")
        val outcome = ValidationResult.invalid(issue).toOutcome("input rejected")
        assertSame(StandardOutcomes.INVALID_ARGUMENT, outcome.definition)
        assertEquals(listOf(issue), outcome.issues)
        assertEquals("input rejected", outcome.detail)
    }

    @Test fun invalidHasValueEquality() {
        assertEquals(ValidationResult.invalid(Issue.of("Bad.")), ValidationResult.invalid(Issue.of("Bad.")))
        assertEquals(
            ValidationResult.invalid(Issue.of("Bad.")).hashCode(),
            ValidationResult.invalid(Issue.of("Bad.")).hashCode(),
        )
    }

    @Test fun invalidToStringContainsIssues() {
        assertTrue(ValidationResult.invalid(Issue.of("Bad.")).toString().contains("Bad."))
    }
}

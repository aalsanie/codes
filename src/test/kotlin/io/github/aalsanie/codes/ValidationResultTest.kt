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

    @Test fun validConvertsToOkWithExplicitFailureDefinition() {
        val outcome = ValidationResult.valid().toOutcome(StandardOutcomes.INVALID_ARGUMENT)
        assertSame(StandardOutcomes.OK, outcome.definition)
    }

    @Test fun invalidConvertsToExplicitFailureDefinition() {
        val issue = Issue.of("Bad.")
        val outcome =
            ValidationResult
                .invalid(issue)
                .toOutcome(StandardOutcomes.FAILED_PRECONDITION, "validation rejected")
        assertSame(StandardOutcomes.FAILED_PRECONDITION, outcome.definition)
        assertEquals(listOf(issue), outcome.issues)
        assertEquals("validation rejected", outcome.detail)
    }

    @Test fun invalidCanUseCustomFailureDefinition() {
        val definition =
            OutcomeDefinition.custom(
                "com.example.validation",
                "ORDER_NOT_MODIFIABLE",
                OutcomeState.FAILED,
                "The order cannot be modified.",
            )
        val outcome = ValidationResult.invalid(Issue.of("Locked.")).toOutcome(definition)
        assertSame(definition, outcome.definition)
    }

    @Test fun conversionRejectsNonFailureDefinitionDeterministically() {
        val pending =
            OutcomeDefinition.custom(
                "com.example.validation",
                "VALIDATION_PENDING",
                OutcomeState.PENDING,
                "Validation is pending.",
            )

        assertFails<IllegalArgumentException> {
            ValidationResult.valid().toOutcome(StandardOutcomes.OK)
        }
        assertFails<IllegalArgumentException> {
            ValidationResult.invalid(Issue.of("Bad.")).toOutcome(pending)
        }
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

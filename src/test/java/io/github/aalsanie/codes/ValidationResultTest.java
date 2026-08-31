package io.github.aalsanie.codes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationResultTest {
    @Test
    void validIsSingletonAndConvertsToOk() {
        ValidationResult valid = ValidationResult.valid();
        assertSame(valid, ValidationResult.valid());
        assertTrue(valid.isValid());
        assertFalse(valid.isInvalid());
        assertTrue(valid.issues().isEmpty());
        assertEquals("Valid", valid.toString());
        assertEquals(StandardOutcomes.OK, valid.toOutcome(StandardOutcomes.INVALID_ARGUMENT).getDefinition());
    }

    @Test
    void invalidCopiesIssuesAndConvertsToExplicitFailure() {
        ArrayList<Issue> issues = new ArrayList<>();
        issues.add(Issue.at("email", "Invalid."));
        ValidationResult result = ValidationResult.invalid(issues);
        issues.clear();

        assertTrue(result.isInvalid());
        assertEquals(1, result.issues().size());
        assertEquals(result, ValidationResult.invalid(Issue.at("email", "Invalid.")));
        assertEquals(result.hashCode(), ValidationResult.invalid(Issue.at("email", "Invalid.")).hashCode());
        assertTrue(result.toString().contains("Invalid"));

        Outcome outcome = result.toOutcome(StandardOutcomes.INVALID_ARGUMENT, "request validation failed");
        assertEquals(StandardOutcomes.INVALID_ARGUMENT, outcome.getDefinition());
        assertEquals("request validation failed", outcome.getDetail());
        assertEquals(result.issues(), outcome.getIssues());
    }

    @Test
    void combinesIndependentResults() {
        ValidationResult result = ValidationResult.combine(
            ValidationResult.valid(),
            ValidationResult.invalid(Issue.at("email", "Invalid.")),
            ValidationResult.invalid(Issue.at("name", "Required."))
        );
        assertEquals(2, result.issues().size());
        assertTrue(ValidationResult.combine(List.of()).isValid());
        assertThrows(NullPointerException.class, () -> ValidationResult.combine((ValidationResult[]) null));
        assertThrows(NullPointerException.class, () -> ValidationResult.combine((List<ValidationResult>) null));
        assertThrows(NullPointerException.class, () -> ValidationResult.combine(List.of(ValidationResult.valid(), null)));
    }

    @Test
    void rejectsInvalidPoliciesAndEmptyInvalidResults() {
        OutcomeDefinition success = OutcomeDefinition.custom(
            "com.example", "CREATED", OutcomeState.SUCCEEDED, "Created."
        );
        assertThrows(IllegalArgumentException.class, () -> ValidationResult.valid().toOutcome(success));
        assertThrows(IllegalArgumentException.class, () -> ValidationResult.invalid(List.of()));
        assertThrows(NullPointerException.class, () -> ValidationResult.invalid((Issue) null));
        assertThrows(NullPointerException.class, () -> ValidationResult.invalid((List<Issue>) null));
        assertThrows(NullPointerException.class, () -> ValidationResult.valid().toOutcome(null));
    }
}

package io.github.aalsanie.codes;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IssueOutcomeTest {
    @Test
    void createsIssueVariantsWithValueEquality() {
        OutcomeCode code = OutcomeCode.of("com.example.validation", "INVALID_EMAIL");
        assertEquals(Issue.of("Invalid."), Issue.of("Invalid."));
        assertEquals("Invalid.", Issue.of("Invalid.").toString());
        assertEquals("email: Invalid.", Issue.at("email", "Invalid.").toString());
        assertEquals(code + " Invalid.", Issue.coded(code, "Invalid.").toString());
        assertEquals(code + " email: Invalid.", Issue.at("email", code, "Invalid.").toString());
        assertEquals(code, Issue.at("email", code, "Invalid.").getCode());
        assertEquals("email", Issue.at("email", code, "Invalid.").getPath());
        assertNull(Issue.of("Invalid.").getCode());
        assertNull(Issue.of("Invalid.").getPath());
        Issue issue = Issue.at("email", code, "Invalid.");

        assertEquals(issue, issue);
        assertNotEquals("not-an-issue", issue);
        assertNotEquals(
            issue,
            Issue.at(
                "email",
                OutcomeCode.of("com.example.validation", "OTHER"),
                "Invalid."
            )
        );
        assertNotEquals(issue, Issue.at("name", code, "Invalid."));
        assertNotEquals(issue, Issue.at("email", code, "Different."));
        assertEquals(
            issue.hashCode(),
            Issue.at("email", code, "Invalid.").hashCode()
        );
    }

    @Test
    void rejectsInvalidIssueTextAndPath() {
        assertThrows(NullPointerException.class, () -> Issue.of(null));
        assertThrows(IllegalArgumentException.class, () -> Issue.of(" "));
        assertThrows(IllegalArgumentException.class, () -> Issue.of("x".repeat(1025)));
        assertThrows(IllegalArgumentException.class, () -> Issue.of("bad\0message"));
        assertThrows(IllegalArgumentException.class, () -> Issue.at(" ", "Bad."));
        assertThrows(IllegalArgumentException.class, () -> Issue.at("x".repeat(257), "Bad."));
        assertThrows(IllegalArgumentException.class, () -> Issue.at("bad\0path", "Bad."));
        assertThrows(NullPointerException.class, () -> Issue.coded(null, "Bad."));
        assertThrows(NullPointerException.class, () -> Issue.at("field", null, "Bad."));
        assertThrows(NullPointerException.class, () -> Issue.at(null, "Bad."));
        assertThrows(
            NullPointerException.class,
            () -> Issue.at(null, OutcomeCode.of("com.example", "BAD"), "Bad.")
        );
    }

    @Test
    void outcomeDerivesSemanticsAndDefensivelyCopiesIssues() {
        ArrayList<Issue> source = new ArrayList<>();
        source.add(Issue.at("email", "Invalid."));
        Outcome outcome = Outcome.of(StandardOutcomes.INVALID_ARGUMENT, "request=42", source);
        source.clear();

        assertEquals(StandardOutcomes.INVALID_ARGUMENT, outcome.getDefinition());
        assertEquals(StandardOutcomes.INVALID_ARGUMENT.getCode(), outcome.getCode());
        assertEquals(OutcomeState.FAILED, outcome.getState());
        assertEquals(outcome.getDefaultMessage(), outcome.getMessage());
        assertEquals("request=42", outcome.getDetail());
        assertEquals(1, outcome.getIssues().size());
        assertTrue(outcome.isFailed());
        assertFalse(outcome.isSuccessful());
        assertFalse(outcome.isPending());
        assertTrue(outcome.isTerminal());
        assertFalse(outcome.toString().contains("request=42"));
        assertThrows(UnsupportedOperationException.class, () -> outcome.getIssues().add(Issue.of("x")));
    }

    @Test
    void successPendingAndValidationRulesAreEnforced() {
        Outcome success = Outcome.of(StandardOutcomes.OK);
        assertTrue(success.isSuccessful());
        assertTrue(success.isTerminal());
        assertNull(success.getDetail());

        OutcomeDefinition pendingDefinition = OutcomeDefinition.custom(
            "com.example.jobs", "JOB_PROCESSING", OutcomeState.PENDING, "Job processing."
        );
        Outcome pending = Outcome.of(pendingDefinition, "job=7");
        assertTrue(pending.isPending());
        assertFalse(pending.isTerminal());

        assertThrows(
            IllegalArgumentException.class,
            () -> Outcome.of(StandardOutcomes.OK, null, List.of(Issue.of("No.")))
        );
        assertThrows(IllegalArgumentException.class, () -> Outcome.of(StandardOutcomes.INTERNAL, " "));
        assertThrows(IllegalArgumentException.class, () -> Outcome.of(StandardOutcomes.INTERNAL, "x".repeat(4097)));
        assertThrows(IllegalArgumentException.class, () -> Outcome.of(StandardOutcomes.INTERNAL, "bad\0detail"));
        assertThrows(NullPointerException.class, () -> Outcome.of(null));
        assertThrows(NullPointerException.class, () -> Outcome.of(StandardOutcomes.INTERNAL, null, null));
    }
}

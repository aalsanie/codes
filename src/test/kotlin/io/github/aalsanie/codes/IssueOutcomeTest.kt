package io.github.aalsanie.codes

import org.junit.jupiter.api.Test

class IssueOutcomeTest {
    private val pendingDefinition =
        OutcomeDefinition.custom(
            "com.example.jobs",
            "JOB_PROCESSING",
            OutcomeState.PENDING,
            "The job is processing.",
        )

    @Test fun createsMessageOnlyIssue() {
        val issue = Issue.of("Invalid value.")
        assertNull(issue.code)
        assertNull(issue.path)
        assertEquals("Invalid value.", issue.message)
    }

    @Test fun createsCodedIssue() {
        val code = OutcomeCode.of("com.example", "INVALID_EMAIL")
        val issue = Issue.coded(code, "Invalid email.")
        assertEquals(code, issue.code)
        assertEquals("com.example:INVALID_EMAIL Invalid email.", issue.toString())
    }

    @Test fun createsPathIssue() {
        val issue = Issue.at("user.email", "Invalid email.")
        assertEquals("user.email", issue.path)
        assertEquals("user.email: Invalid email.", issue.toString())
    }

    @Test fun createsPathAndCodeIssue() {
        val code = OutcomeCode.of("com.example", "INVALID_EMAIL")
        val issue = Issue.at("user.email", code, "Invalid email.")
        assertEquals("com.example:INVALID_EMAIL user.email: Invalid email.", issue.toString())
    }

    @Test fun issueHasValueEquality() {
        assertEquals(Issue.at("a", "Bad."), Issue.at("a", "Bad."))
        assertEquals(Issue.at("a", "Bad.").hashCode(), Issue.at("a", "Bad.").hashCode())
        assertNotEquals(Issue.at("a", "Bad."), Issue.at("b", "Bad."))
    }

    @Test fun rejectsBlankIssueMessage() {
        assertFails<IllegalArgumentException> { Issue.of(" ") }
    }

    @Test fun rejectsNulIssueMessage() {
        assertFails<IllegalArgumentException> { Issue.of("bad\u0000message") }
    }

    @Test fun rejectsBlankIssuePath() {
        assertFails<IllegalArgumentException> { Issue.at(" ", "Bad.") }
    }

    @Test fun rejectsOversizedIssuePath() {
        assertFails<IllegalArgumentException> { Issue.at("x".repeat(257), "Bad.") }
    }

    @Test fun rejectsNulIssuePath() {
        assertFails<IllegalArgumentException> { Issue.at("x\u0000y", "Bad.") }
    }

    @Test fun createsSuccessfulOutcome() {
        val outcome = Outcome.of(StandardOutcomes.OK)
        assertTrue(outcome.isSuccessful)
        assertFalse(outcome.isPending)
        assertFalse(outcome.isFailed)
        assertTrue(outcome.isTerminal)
        assertEquals(StandardOutcomes.OK.defaultMessage, outcome.message)
    }

    @Test fun createsPendingOutcomeFromApplicationDefinition() {
        val outcome = Outcome.of(pendingDefinition)
        assertFalse(outcome.isSuccessful)
        assertTrue(outcome.isPending)
        assertFalse(outcome.isFailed)
        assertFalse(outcome.isTerminal)
    }

    @Test fun createsFailedOutcomeWithIssues() {
        val issue = Issue.at("name", "Required.")
        val outcome = Outcome.of(StandardOutcomes.INVALID_ARGUMENT, issues = listOf(issue))
        assertTrue(outcome.isFailed)
        assertEquals(listOf(issue), outcome.issues)
    }

    @Test fun detailDoesNotReplaceStableMessage() {
        val outcome = Outcome.of(StandardOutcomes.NOT_FOUND, detail = "customer 1234 was missing")
        assertEquals(StandardOutcomes.NOT_FOUND.defaultMessage, outcome.message)
        assertEquals("customer 1234 was missing", outcome.detail)
        assertFalse(outcome.toString().contains("customer 1234"))
    }

    @Test fun rejectsIssuesOnSuccessfulOutcome() {
        assertFails<IllegalArgumentException> { Outcome.of(StandardOutcomes.OK, issues = listOf(Issue.of("Bad."))) }
    }

    @Test fun rejectsIssuesOnPendingOutcome() {
        assertFails<IllegalArgumentException> { Outcome.of(pendingDefinition, issues = listOf(Issue.of("Bad."))) }
    }

    @Test fun rejectsBlankDetail() {
        assertFails<IllegalArgumentException> { Outcome.of(StandardOutcomes.OK, " ") }
    }

    @Test fun rejectsOversizedDetail() {
        assertFails<IllegalArgumentException> { Outcome.of(StandardOutcomes.OK, "x".repeat(4097)) }
    }

    @Test fun rejectsNulDetail() {
        assertFails<IllegalArgumentException> { Outcome.of(StandardOutcomes.OK, "x\u0000y") }
    }

    @Test fun outcomeIssuesAreDefensiveCopy() {
        val source = mutableListOf(Issue.of("Bad."))
        val outcome = Outcome.of(StandardOutcomes.INTERNAL, issues = source)
        source.clear()
        assertEquals(1, outcome.issues.size)
    }

    @Test fun outcomeIssuesAreUnmodifiable() {
        val outcome = Outcome.of(StandardOutcomes.INTERNAL, issues = listOf(Issue.of("Bad.")))

        @Suppress("UNCHECKED_CAST")
        val mutable = outcome.issues as MutableList<Issue>
        assertFails<UnsupportedOperationException> { mutable.clear() }
    }
}

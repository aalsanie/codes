package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class OutcomeProblemDetailMapperTest {
    @Test
    void safeDefaultsExposeOnlyStatusAndMachineCode() {
        Outcome outcome = Outcome.of(
            StandardOutcomes.INVALID_ARGUMENT,
            "internal request id=42",
            List.of(Issue.at("email", "Invalid email."))
        );
        ProblemDetail problem = OutcomeProblemDetailMapper.safeDefaults().map(outcome).orNull();
        assertEquals(400, problem.getStatus());
        assertEquals("Bad Request", problem.getTitle());
        assertNull(problem.getDetail());
        assertEquals(outcome.getCode().getValue(), problem.getProperties().get("code"));
        assertFalse(problem.getProperties().containsKey("outcomeDetail"));
        assertFalse(problem.getProperties().containsKey("issues"));
    }

    @Test
    void explicitExposureCanPublishMessageDetailAndIssues() {
        Outcome outcome = Outcome.of(
            StandardOutcomes.INVALID_ARGUMENT,
            "request id=42",
            List.of(Issue.at("email", "Invalid email."))
        );
        OutcomeProblemDetailMapper mapper = new OutcomeProblemDetailMapper(
            SpringHttpStatusMapper.standard(),
            new SpringOutcomeExposure(true, true, true)
        );
        ProblemDetail problem = mapper.map(outcome).orNull();
        assertEquals(outcome.getMessage(), problem.getDetail());
        assertEquals("request id=42", problem.getProperties().get("outcomeDetail"));
        List<?> issues = (List<?>) problem.getProperties().get("issues");
        assertEquals(1, issues.size());
        Map<?, ?> issue = (Map<?, ?>) issues.get(0);
        assertEquals("email", issue.get("path"));
        assertEquals("Invalid email.", issue.get("message"));
    }

    @Test
    void preservesExplicitUnmappedResultAndRejectsSuccessProblems() {
        assertTrue(OutcomeProblemDetailMapper.safeDefaults().map(Outcome.of(StandardOutcomes.FAILED_PRECONDITION)).isUnmapped());
        assertThrows(
            IllegalArgumentException.class,
            () -> OutcomeProblemDetailMapper.safeDefaults().map(Outcome.of(StandardOutcomes.OK))
        );
    }

    @Test
    void namedExposurePoliciesRemainConservative() {
        assertEquals(new SpringOutcomeExposure(false, false, false), SpringOutcomeExposure.safeDefaults());
        assertEquals(new SpringOutcomeExposure(true, false, true), SpringOutcomeExposure.publicErrors());
    }
}

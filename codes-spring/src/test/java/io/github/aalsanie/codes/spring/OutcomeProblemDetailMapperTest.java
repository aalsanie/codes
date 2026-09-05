package io.github.aalsanie.codes.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.Outcome;
import io.github.aalsanie.codes.StandardOutcomes;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class OutcomeProblemDetailMapperTest {
    private static final URI ABOUT_BLANK = URI.create("about:blank");

    @Test
    void safeDefaultsExposeOnlyRfcDefaultsAndMachineCode() {
        Outcome outcome = Outcome.of(
            StandardOutcomes.NOT_FOUND,
            "database shard=7 customerId=42"
        );

        ProblemDetail problem = OutcomeProblemDetailMapper.safeDefaults().map(outcome).orNull();

        assertEquals(404, problem.getStatus());
        assertAboutBlank(problem.getType());
        assertEquals("Not Found", problem.getTitle());
        assertNull(problem.getDetail());
        assertEquals(outcome.getCode().getValue(), problem.getProperties().get("code"));
        assertFalse(problem.getProperties().containsKey("outcomeDetail"));
        assertFalse(problem.getProperties().containsKey("issues"));
    }

    @Test
    void applicationOwnedTypeUsesStableMessageAsTitleAndOccurrenceDetailAsRfcDetail() {
        Outcome outcome = Outcome.of(
            StandardOutcomes.INVALID_ARGUMENT,
            "The email address cannot receive mail.",
            List.of(Issue.at("email", "Invalid email address."))
        );
        URI type = URI.create("https://api.example.test/problems/invalid-argument");
        OutcomeProblemDetailMapper mapper = new OutcomeProblemDetailMapper(
            SpringHttpStatusMapper.standard(),
            new SpringOutcomeExposure(true, true, true),
            SpringProblemTypeUriMapper.empty()
                .withMapping(StandardOutcomes.INVALID_ARGUMENT, type)
        );

        ProblemDetail problem = mapper.map(outcome).orNull();

        assertEquals(type, problem.getType());
        assertEquals(outcome.getMessage(), problem.getTitle());
        assertEquals(outcome.getDetail(), problem.getDetail());
        assertFalse(problem.getProperties().containsKey("outcomeDetail"));

        List<?> issues = (List<?>) problem.getProperties().get("issues");
        assertEquals(1, issues.size());
        Map<?, ?> issue = (Map<?, ?>) issues.get(0);
        assertEquals("email", issue.get("path"));
        assertEquals("Invalid email address.", issue.get("message"));
    }

    @Test
    void applicationOwnedTypeDoesNotReuseHttpStatusTitleWhenMessageIsHidden() {
        Outcome outcome = Outcome.of(
            StandardOutcomes.INVALID_ARGUMENT,
            "sensitive occurrence detail"
        );
        URI type = URI.create("https://api.example.test/problems/invalid-argument");
        OutcomeProblemDetailMapper mapper = new OutcomeProblemDetailMapper(
            SpringHttpStatusMapper.standard(),
            SpringOutcomeExposure.safeDefaults(),
            SpringProblemTypeUriMapper.empty()
                .withMapping(StandardOutcomes.INVALID_ARGUMENT, type)
        );

        ProblemDetail problem = mapper.map(outcome).orNull();

        assertEquals(type, problem.getType());
        assertNull(problem.getTitle());
        assertNull(problem.getDetail());
        assertEquals(outcome.getCode().getValue(), problem.getProperties().get("code"));
    }

    @Test
    void aboutBlankKeepsHttpTitleEvenWhenPublicMessageExposureIsEnabled() {
        Outcome outcome = Outcome.of(StandardOutcomes.INVALID_ARGUMENT);
        OutcomeProblemDetailMapper mapper = new OutcomeProblemDetailMapper(
            SpringHttpStatusMapper.standard(),
            SpringOutcomeExposure.publicErrors()
        );

        ProblemDetail problem = mapper.map(outcome).orNull();

        assertAboutBlank(problem.getType());
        assertEquals("Bad Request", problem.getTitle());
        assertEquals(outcome.getCode().getValue(), problem.getProperties().get("code"));
    }

    @Test
    void preservesExplicitUnmappedResultAndRejectsSuccessProblems() {
        assertTrue(
            OutcomeProblemDetailMapper.safeDefaults()
                .map(Outcome.of(StandardOutcomes.FAILED_PRECONDITION))
                .isUnmapped()
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> OutcomeProblemDetailMapper.safeDefaults().map(Outcome.of(StandardOutcomes.OK))
        );
    }

    @Test
    void namedExposurePoliciesRemainConservative() {
        assertEquals(
            new SpringOutcomeExposure(false, false, false),
            SpringOutcomeExposure.safeDefaults()
        );
        assertEquals(
            new SpringOutcomeExposure(true, false, true),
            SpringOutcomeExposure.publicErrors()
        );
    }

    private static void assertAboutBlank(URI type) {
        assertTrue(type == null || ABOUT_BLANK.equals(type));
    }
}

package io.github.aalsanie.codes.spring;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

public final class OutcomeProblemDetailMapper {
    public static final String CODE_PROPERTY = "code";
    public static final String OUTCOME_DETAIL_PROPERTY = "outcomeDetail";
    public static final String ISSUES_PROPERTY = "issues";

    private final SpringHttpStatusMapper statusMapper;
    private final SpringOutcomeExposure exposure;

    public OutcomeProblemDetailMapper(
        SpringHttpStatusMapper statusMapper,
        SpringOutcomeExposure exposure
    ) {
        this.statusMapper = Objects.requireNonNull(statusMapper, "statusMapper");
        this.exposure = Objects.requireNonNull(exposure, "exposure");
    }

    public static OutcomeProblemDetailMapper safeDefaults() {
        return new OutcomeProblemDetailMapper(
            SpringHttpStatusMapper.standard(),
            SpringOutcomeExposure.safeDefaults()
        );
    }

    public MappingResult<ProblemDetail> map(Outcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        if (!outcome.isFailed()) {
            throw new IllegalArgumentException("ProblemDetail is only produced for failed outcomes");
        }

        return statusMapper.map(outcome).fold(
            status -> MappingResult.mapped(createProblemDetail(outcome, status)),
            MappingResult::unmapped
        );
    }

    private ProblemDetail createProblemDetail(Outcome outcome, HttpStatusCode status) {
        ProblemDetail problem = ProblemDetail.forStatus(status.value());
        problem.setTitle(reasonPhrase(status));
        problem.setProperty(CODE_PROPERTY, outcome.getCode().getValue());

        if (exposure.exposeMessage()) {
            problem.setDetail(outcome.getMessage());
        }
        if (exposure.exposeDetail() && outcome.getDetail() != null) {
            problem.setProperty(OUTCOME_DETAIL_PROPERTY, outcome.getDetail());
        }
        if (exposure.exposeIssues() && !outcome.getIssues().isEmpty()) {
            problem.setProperty(ISSUES_PROPERTY, toIssuePayload(outcome.getIssues()));
        }
        return problem;
    }

    private static String reasonPhrase(HttpStatusCode status) {
        HttpStatus known = HttpStatus.resolve(status.value());
        return known == null ? "HTTP " + status.value() : known.getReasonPhrase();
    }

    private static List<Map<String, String>> toIssuePayload(List<Issue> issues) {
        List<Map<String, String>> values = new ArrayList<>(issues.size());
        for (Issue issue : issues) {
            LinkedHashMap<String, String> value = new LinkedHashMap<>();
            if (issue.getCode() != null) {
                value.put("code", issue.getCode().getValue());
            }
            if (issue.getPath() != null) {
                value.put("path", issue.getPath());
            }
            value.put("message", issue.getMessage());
            values.add(Map.copyOf(value));
        }
        return List.copyOf(values);
    }
}

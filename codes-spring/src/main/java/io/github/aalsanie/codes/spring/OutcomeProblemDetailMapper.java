package io.github.aalsanie.codes.spring;

import io.github.aalsanie.codes.Issue;
import io.github.aalsanie.codes.MappingResult;
import io.github.aalsanie.codes.Outcome;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ProblemDetail;

/**
 * Maps failed Codes outcomes to Spring {@link ProblemDetail} instances.
 *
 * <p>The stable Codes identity is always exposed as {@code code}. An RFC 9457 problem
 * {@code type} is set only when the application explicitly supplies an owned URI mapping.
 * For a mapped problem type, the reusable outcome message may be exposed as the stable
 * {@code title}. Occurrence-specific {@link Outcome#getDetail()} is exposed only as RFC
 * {@code detail}; it is never moved to a custom extension field.
 */
public final class OutcomeProblemDetailMapper {
    public static final String CODE_PROPERTY = "code";
    public static final String ISSUES_PROPERTY = "issues";

    private final SpringHttpStatusMapper statusMapper;
    private final SpringOutcomeExposure exposure;
    private final SpringProblemTypeUriMapper problemTypeMapper;

    public OutcomeProblemDetailMapper(
        SpringHttpStatusMapper statusMapper,
        SpringOutcomeExposure exposure
    ) {
        this(statusMapper, exposure, SpringProblemTypeUriMapper.empty());
    }

    public OutcomeProblemDetailMapper(
        SpringHttpStatusMapper statusMapper,
        SpringOutcomeExposure exposure,
        SpringProblemTypeUriMapper problemTypeMapper
    ) {
        this.statusMapper = Objects.requireNonNull(statusMapper, "statusMapper");
        this.exposure = Objects.requireNonNull(exposure, "exposure");
        this.problemTypeMapper = Objects.requireNonNull(problemTypeMapper, "problemTypeMapper");
    }

    public static OutcomeProblemDetailMapper safeDefaults() {
        return new OutcomeProblemDetailMapper(
            SpringHttpStatusMapper.standard(),
            SpringOutcomeExposure.safeDefaults(),
            SpringProblemTypeUriMapper.empty()
        );
    }

    public MappingResult<ProblemDetail> map(Outcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        if (!outcome.isFailed()) {
            throw new IllegalArgumentException("ProblemDetail is only produced for failed outcomes");
        }

        return statusMapper.map(outcome).fold(
            status -> MappingResult.mapped(createProblemDetail(outcome, status.value())),
            MappingResult::unmapped
        );
    }

    private ProblemDetail createProblemDetail(Outcome outcome, int status) {
        URI problemType = problemTypeMapper.map(outcome).orNull();
        ProblemDetail problem = createProblemDetail(status, problemType);

        problem.setProperty(CODE_PROPERTY, outcome.getCode().getValue());

        if (problemType != null) {
            problem.setType(problemType);
            if (exposure.exposeMessage()) {
                problem.setTitle(outcome.getMessage());
            }
        }

        if (exposure.exposeDetail() && outcome.getDetail() != null) {
            problem.setDetail(outcome.getDetail());
        }
        if (exposure.exposeIssues() && !outcome.getIssues().isEmpty()) {
            problem.setProperty(ISSUES_PROPERTY, toIssuePayload(outcome.getIssues()));
        }
        return problem;
    }

    private ProblemDetail createProblemDetail(int status, @Nullable URI problemType) {
        if (problemType != null && !exposure.exposeMessage()) {
            return new ProblemDetailWithoutDefaultTitle(status);
        }
        return ProblemDetail.forStatus(status);
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

    /**
     * Spring's default {@link ProblemDetail#getTitle()} synthesizes the HTTP reason phrase when
     * no title was explicitly configured. That behavior is correct for implicit {@code about:blank}
     * but not for an application-owned problem type whose stable title is intentionally hidden.
     */
    private static final class ProblemDetailWithoutDefaultTitle extends ProblemDetail {
        private @Nullable String explicitTitle;

        private ProblemDetailWithoutDefaultTitle(int status) {
            super(status);
        }

        @Override
        public void setTitle(@Nullable String title) {
            explicitTitle = title;
        }

        @Override
        public @Nullable String getTitle() {
            return explicitTitle;
        }
    }
}

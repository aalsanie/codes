package io.github.aalsanie.codes;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class Outcome {
    private final OutcomeDefinition definition;
    private final @Nullable String detail;
    private final List<Issue> issues;

    private Outcome(
        OutcomeDefinition definition,
        @Nullable String detail,
        List<Issue> issues
    ) {
        this.definition = definition;
        this.detail = detail;
        this.issues = issues;
    }

    public static Outcome of(OutcomeDefinition definition) {
        return of(definition, null, List.of());
    }

    public static Outcome of(
        OutcomeDefinition definition,
        @Nullable String detail
    ) {
        return of(definition, detail, List.of());
    }

    public static Outcome of(
        OutcomeDefinition definition,
        @Nullable String detail,
        List<Issue> issues
    ) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(issues, "issues");
        if (detail != null) {
            Constraints.requireDetail(detail);
        }
        if (definition.getState() != OutcomeState.FAILED && !issues.isEmpty()) {
            throw new IllegalArgumentException("issues are only allowed on failed outcomes");
        }
        return new Outcome(definition, detail, List.copyOf(issues));
    }

    public OutcomeDefinition getDefinition() {
        return definition;
    }

    public OutcomeCode getCode() {
        return definition.getCode();
    }

    public OutcomeState getState() {
        return definition.getState();
    }

    public String getDefaultMessage() {
        return definition.getDefaultMessage();
    }

    public String getMessage() {
        return getDefaultMessage();
    }

    public @Nullable String getDetail() {
        return detail;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public boolean isSuccessful() {
        return getState() == OutcomeState.SUCCEEDED;
    }

    public boolean isPending() {
        return getState() == OutcomeState.PENDING;
    }

    public boolean isFailed() {
        return getState() == OutcomeState.FAILED;
    }

    public boolean isTerminal() {
        return getState() != OutcomeState.PENDING;
    }

    @Override
    public String toString() {
        return "Outcome(code=" + getCode()
            + ", state=" + getState()
            + ", message=" + getMessage()
            + ", issues=" + issues.size()
            + ")";
    }
}

package io.github.aalsanie.codes;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract sealed class ValidationResult permits ValidationResult.Valid, ValidationResult.Invalid {
    private ValidationResult() {
    }

    public static ValidationResult valid() {
        return Valid.INSTANCE;
    }

    public static ValidationResult invalid(Issue issue) {
        return new Invalid(List.of(Objects.requireNonNull(issue, "issue")));
    }

    public static ValidationResult invalid(List<Issue> issues) {
        return new Invalid(issues);
    }

    public static ValidationResult combine(ValidationResult... results) {
        Objects.requireNonNull(results, "results");
        return combine(List.of(results));
    }

    public static ValidationResult combine(List<ValidationResult> results) {
        Objects.requireNonNull(results, "results");
        List<Issue> issues = new ArrayList<>();
        for (ValidationResult result : results) {
            Objects.requireNonNull(result, "result");
            issues.addAll(result.issues());
        }
        return issues.isEmpty() ? Valid.INSTANCE : new Invalid(issues);
    }

    public final boolean isValid() {
        return this == Valid.INSTANCE;
    }

    public final boolean isInvalid() {
        return !isValid();
    }

    public abstract List<Issue> issues();

    public final Outcome toOutcome(OutcomeDefinition failureDefinition) {
        return toOutcome(failureDefinition, null);
    }

    public final Outcome toOutcome(
        OutcomeDefinition failureDefinition,
        @Nullable String detail
    ) {
        Objects.requireNonNull(failureDefinition, "failureDefinition");
        if (failureDefinition.getState() != OutcomeState.FAILED) {
            throw new IllegalArgumentException("validation failure definition must have FAILED state");
        }
        if (isValid()) {
            return Outcome.of(StandardOutcomes.OK, detail);
        }
        return Outcome.of(failureDefinition, detail, issues());
    }

    public static final class Valid extends ValidationResult {
        private static final Valid INSTANCE = new Valid();

        private Valid() {
        }

        @Override
        public List<Issue> issues() {
            return List.of();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Valid;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "Valid";
        }
    }

    public static final class Invalid extends ValidationResult {
        private final List<Issue> issues;

        private Invalid(List<Issue> issues) {
            Objects.requireNonNull(issues, "issues");
            if (issues.isEmpty()) {
                throw new IllegalArgumentException("invalid validation result requires at least one issue");
            }
            this.issues = List.copyOf(issues);
        }

        @Override
        public List<Issue> issues() {
            return issues;
        }

        public List<Issue> getIssues() {
            return issues;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof Invalid invalid && issues.equals(invalid.issues));
        }

        @Override
        public int hashCode() {
            return issues.hashCode();
        }

        @Override
        public String toString() {
            return "Invalid(issues=" + issues + ")";
        }
    }
}

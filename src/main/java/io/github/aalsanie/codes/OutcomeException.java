package io.github.aalsanie.codes;

import java.util.Objects;

public final class OutcomeException extends RuntimeException {
    private final Outcome outcome;

    public OutcomeException(Outcome outcome) {
        this(outcome, null);
    }

    public OutcomeException(Outcome outcome, Throwable cause) {
        super(requireFailed(outcome).getMessage(), cause);
        this.outcome = outcome;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    private static Outcome requireFailed(Outcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        if (!outcome.isFailed()) {
            throw new IllegalArgumentException("only failed outcomes can be converted to OutcomeException");
        }
        return outcome;
    }
}

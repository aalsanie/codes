package io.github.aalsanie.codes;

public final class OutcomeExceptions {
    private OutcomeExceptions() {
    }

    public static OutcomeException toException(Outcome outcome) {
        return new OutcomeException(outcome);
    }

    public static OutcomeException toException(Outcome outcome, Throwable cause) {
        return new OutcomeException(outcome, cause);
    }
}

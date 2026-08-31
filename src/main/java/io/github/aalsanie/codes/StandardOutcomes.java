package io.github.aalsanie.codes;

import java.util.List;

public final class StandardOutcomes {
    public static final String NAMESPACE = "io.github.aalsanie.codes.standard";

    public static final OutcomeDefinition OK = OutcomeDefinition.standard("OK");
    public static final OutcomeDefinition INVALID_ARGUMENT = OutcomeDefinition.standard("INVALID_ARGUMENT");
    public static final OutcomeDefinition UNAUTHENTICATED = OutcomeDefinition.standard("UNAUTHENTICATED");
    public static final OutcomeDefinition PERMISSION_DENIED = OutcomeDefinition.standard("PERMISSION_DENIED");
    public static final OutcomeDefinition NOT_FOUND = OutcomeDefinition.standard("NOT_FOUND");
    public static final OutcomeDefinition ALREADY_EXISTS = OutcomeDefinition.standard("ALREADY_EXISTS");
    public static final OutcomeDefinition FAILED_PRECONDITION = OutcomeDefinition.standard("FAILED_PRECONDITION");
    public static final OutcomeDefinition OUT_OF_RANGE = OutcomeDefinition.standard("OUT_OF_RANGE");
    public static final OutcomeDefinition RATE_LIMITED = OutcomeDefinition.standard("RATE_LIMITED");
    public static final OutcomeDefinition CANCELLED = OutcomeDefinition.standard("CANCELLED");
    public static final OutcomeDefinition DEADLINE_EXCEEDED = OutcomeDefinition.standard("DEADLINE_EXCEEDED");
    public static final OutcomeDefinition ABORTED = OutcomeDefinition.standard("ABORTED");
    public static final OutcomeDefinition UNIMPLEMENTED = OutcomeDefinition.standard("UNIMPLEMENTED");
    public static final OutcomeDefinition UNAVAILABLE = OutcomeDefinition.standard("UNAVAILABLE");
    public static final OutcomeDefinition INTERNAL = OutcomeDefinition.standard("INTERNAL");
    public static final OutcomeDefinition DATA_LOSS = OutcomeDefinition.standard("DATA_LOSS");
    public static final OutcomeDefinition RESOURCE_EXHAUSTED = OutcomeDefinition.standard("RESOURCE_EXHAUSTED");

    public static final List<OutcomeDefinition> all = List.of(
        OK,
        INVALID_ARGUMENT,
        UNAUTHENTICATED,
        PERMISSION_DENIED,
        NOT_FOUND,
        ALREADY_EXISTS,
        FAILED_PRECONDITION,
        OUT_OF_RANGE,
        RATE_LIMITED,
        CANCELLED,
        DEADLINE_EXCEEDED,
        ABORTED,
        UNIMPLEMENTED,
        UNAVAILABLE,
        INTERNAL,
        DATA_LOSS,
        RESOURCE_EXHAUSTED
    );

    private StandardOutcomes() {
    }
}

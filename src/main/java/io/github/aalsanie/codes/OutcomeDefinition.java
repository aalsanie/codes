package io.github.aalsanie.codes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class OutcomeDefinition {
    static final String ROOT_NAMESPACE = "io.github.aalsanie.codes";
    static final String STANDARD_NAMESPACE = "io.github.aalsanie.codes.standard";

    private static final Map<String, OutcomeDefinition> STANDARD_DEFINITIONS = createStandardDefinitions();

    private final OutcomeCode code;
    private final OutcomeState state;
    private final String defaultMessage;

    private OutcomeDefinition(OutcomeCode code, OutcomeState state, String defaultMessage) {
        this.code = code;
        this.state = state;
        this.defaultMessage = defaultMessage;
    }

    public static OutcomeDefinition custom(
        String namespace,
        String name,
        OutcomeState state,
        String defaultMessage
    ) {
        return custom(OutcomeCode.of(namespace, name), state, defaultMessage);
    }

    public static OutcomeDefinition custom(
        OutcomeCode code,
        OutcomeState state,
        String defaultMessage
    ) {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(state, "state");
        if (isReservedNamespace(code.getNamespace())) {
            throw new IllegalArgumentException("namespace '" + code.getNamespace() + "' is reserved");
        }
        Constraints.requireMessage(defaultMessage);
        return new OutcomeDefinition(code, state, defaultMessage);
    }

    static OutcomeDefinition standard(String name) {
        OutcomeDefinition definition = STANDARD_DEFINITIONS.get(name);
        if (definition == null) {
            throw new IllegalStateException("unknown standard outcome: " + name);
        }
        return definition;
    }

    public OutcomeCode getCode() {
        return code;
    }

    public OutcomeState getState() {
        return state;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return "OutcomeDefinition(code=" + code + ", state=" + state + ", defaultMessage=" + defaultMessage + ")";
    }

    private static Map<String, OutcomeDefinition> createStandardDefinitions() {
        LinkedHashMap<String, OutcomeDefinition> definitions = new LinkedHashMap<>();
        add(definitions, "OK", OutcomeState.SUCCEEDED, "The operation completed successfully.");
        add(definitions, "INVALID_ARGUMENT", OutcomeState.FAILED, "An argument supplied to the operation is invalid.");
        add(definitions, "UNAUTHENTICATED", OutcomeState.FAILED, "Authentication is required or invalid.");
        add(definitions, "PERMISSION_DENIED", OutcomeState.FAILED, "The authenticated caller does not have permission.");
        add(definitions, "NOT_FOUND", OutcomeState.FAILED, "The requested resource was not found.");
        add(definitions, "ALREADY_EXISTS", OutcomeState.FAILED, "The resource already exists.");
        add(definitions, "FAILED_PRECONDITION", OutcomeState.FAILED, "A required precondition was not satisfied.");
        add(definitions, "OUT_OF_RANGE", OutcomeState.FAILED, "A value is outside the allowed range.");
        add(definitions, "RATE_LIMITED", OutcomeState.FAILED, "The caller exceeded an allowed operation rate.");
        add(definitions, "CANCELLED", OutcomeState.FAILED, "The operation was cancelled before completion.");
        add(definitions, "DEADLINE_EXCEEDED", OutcomeState.FAILED, "The operation exceeded its deadline.");
        add(definitions, "ABORTED", OutcomeState.FAILED, "The operation was aborted before completion.");
        add(definitions, "UNIMPLEMENTED", OutcomeState.FAILED, "The requested operation is not implemented.");
        add(definitions, "UNAVAILABLE", OutcomeState.FAILED, "The service is temporarily unavailable.");
        add(definitions, "INTERNAL", OutcomeState.FAILED, "The service encountered an internal error.");
        add(definitions, "DATA_LOSS", OutcomeState.FAILED, "Unrecoverable data loss or corruption was detected.");
        add(definitions, "RESOURCE_EXHAUSTED", OutcomeState.FAILED, "A required resource limit was exhausted.");
        return Collections.unmodifiableMap(definitions);
    }

    private static void add(
        Map<String, OutcomeDefinition> definitions,
        String name,
        OutcomeState state,
        String defaultMessage
    ) {
        Constraints.requireMessage(defaultMessage);
        definitions.put(
            name,
            new OutcomeDefinition(OutcomeCode.of(STANDARD_NAMESPACE, name), state, defaultMessage)
        );
    }

    private static boolean isReservedNamespace(String namespace) {
        return namespace.equals(ROOT_NAMESPACE) || namespace.startsWith(ROOT_NAMESPACE + ".");
    }
}

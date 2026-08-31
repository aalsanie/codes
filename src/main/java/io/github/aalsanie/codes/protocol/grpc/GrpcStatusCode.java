package io.github.aalsanie.codes.protocol.grpc;

import java.util.HashMap;
import java.util.Map;

public enum GrpcStatusCode {
    OK(0),
    CANCELLED(1),
    UNKNOWN(2),
    INVALID_ARGUMENT(3),
    DEADLINE_EXCEEDED(4),
    NOT_FOUND(5),
    ALREADY_EXISTS(6),
    PERMISSION_DENIED(7),
    RESOURCE_EXHAUSTED(8),
    FAILED_PRECONDITION(9),
    ABORTED(10),
    OUT_OF_RANGE(11),
    UNIMPLEMENTED(12),
    INTERNAL(13),
    UNAVAILABLE(14),
    DATA_LOSS(15),
    UNAUTHENTICATED(16);

    private static final Map<Integer, GrpcStatusCode> BY_VALUE = createByValue();

    private final int value;

    GrpcStatusCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GrpcStatusCode fromValue(int value) {
        return BY_VALUE.get(value);
    }

    private static Map<Integer, GrpcStatusCode> createByValue() {
        HashMap<Integer, GrpcStatusCode> values = new HashMap<>();
        for (GrpcStatusCode status : values()) {
            values.put(status.value, status);
        }
        return Map.copyOf(values);
    }
}

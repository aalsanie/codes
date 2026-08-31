package io.github.aalsanie.codes.grpc;

public record GrpcOutcomeExposure(
    boolean exposeMessage,
    boolean exposeDetail,
    boolean exposeIssues
) {
    public static GrpcOutcomeExposure safeDefaults() {
        return new GrpcOutcomeExposure(false, false, false);
    }

    public static GrpcOutcomeExposure publicErrors() {
        return new GrpcOutcomeExposure(true, false, true);
    }
}

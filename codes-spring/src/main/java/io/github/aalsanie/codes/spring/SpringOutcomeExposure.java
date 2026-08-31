package io.github.aalsanie.codes.spring;

public record SpringOutcomeExposure(
    boolean exposeMessage,
    boolean exposeDetail,
    boolean exposeIssues
) {
    public static SpringOutcomeExposure safeDefaults() {
        return new SpringOutcomeExposure(false, false, false);
    }

    public static SpringOutcomeExposure publicErrors() {
        return new SpringOutcomeExposure(true, false, true);
    }
}

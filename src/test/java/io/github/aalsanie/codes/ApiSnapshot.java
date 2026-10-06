package io.github.aalsanie.codes;

import io.github.aalsanie.codes.testing.PublicApiSnapshot;

final class ApiSnapshot {
    private ApiSnapshot() {
    }

    static String create() {
        return PublicApiSnapshot.create(ProblemType.class, "io.github.aalsanie.codes");
    }
}

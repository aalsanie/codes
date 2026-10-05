package io.github.aalsanie.codes;

import java.net.URI;

public interface ProblemType {

    URI getType();

    int getStatus();

    String getTitle();

    static ProblemType of(URI type, int status, String title) {
        return DefaultProblemType.create(type, status, title);
    }
}

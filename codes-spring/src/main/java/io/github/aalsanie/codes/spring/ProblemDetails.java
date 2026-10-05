package io.github.aalsanie.codes.spring;

import io.github.aalsanie.codes.ProblemType;
import java.util.Objects;
import org.springframework.http.ProblemDetail;

public final class ProblemDetails {
    private ProblemDetails() {
    }

    public static ProblemDetail forType(ProblemType problemType) {
        Objects.requireNonNull(problemType, "problemType");
        ProblemDetail problemDetail = ProblemDetail.forStatus(problemType.getStatus());
        problemDetail.setType(problemType.getType());
        problemDetail.setTitle(problemType.getTitle());
        return problemDetail;
    }

    public static ProblemDetail forTypeAndDetail(ProblemType problemType, String detail) {
        Objects.requireNonNull(detail, "detail");
        ProblemDetail problemDetail = forType(problemType);
        problemDetail.setDetail(detail);
        return problemDetail;
    }
}

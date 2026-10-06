package io.github.aalsanie.codes.spring;

import io.github.aalsanie.codes.ProblemType;
import java.util.Objects;
import org.springframework.http.ProblemDetail;

/**
 * Creates Spring {@link ProblemDetail} instances from reusable {@link ProblemType} definitions.
 */
public final class ProblemDetails {
    private ProblemDetails() {
    }

    /**
     * Creates a problem occurrence from the stable type, status, and title.
     *
     * @param problemType reusable problem type definition
     * @return a new Spring problem detail
     * @throws NullPointerException if {@code problemType} is {@code null}
     */
    public static ProblemDetail forType(ProblemType problemType) {
        Objects.requireNonNull(problemType, "problemType");
        ProblemDetail problemDetail = ProblemDetail.forStatus(problemType.getStatus());
        problemDetail.setType(problemType.getType());
        problemDetail.setTitle(problemType.getTitle());
        return problemDetail;
    }

    /**
     * Creates a problem occurrence with occurrence-specific detail.
     *
     * @param problemType reusable problem type definition
     * @param detail client-visible detail for this occurrence
     * @return a new Spring problem detail
     * @throws NullPointerException if either argument is {@code null}
     */
    public static ProblemDetail forTypeAndDetail(ProblemType problemType, String detail) {
        Objects.requireNonNull(detail, "detail");
        ProblemDetail problemDetail = forType(problemType);
        problemDetail.setDetail(detail);
        return problemDetail;
    }
}

package io.github.aalsanie.codes;

import java.net.URI;
import java.util.Objects;

/**
 * Immutable definition of a reusable RFC 9457 problem type.
 */
public final class ProblemType {
    private static final String ABOUT_SCHEME = "about";
    private static final String BLANK_SCHEME_SPECIFIC_PART = "blank";

    private final URI type;
    private final int status;
    private final String title;

    private ProblemType(URI type, int status, String title) {
        this.type = type;
        this.status = status;
        this.title = title;
    }

    /**
     * Creates a problem type.
     *
     * @param type absolute problem type URI; must not be {@code about:blank}
     * @param status HTTP status from 100 through 599
     * @param title non-blank human-readable title
     * @return the problem type
     * @throws NullPointerException if {@code type} or {@code title} is {@code null}
     * @throws IllegalArgumentException if an argument violates the problem type contract
     */
    public static ProblemType of(URI type, int status, String title) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(title, "title");

        if (!type.isAbsolute()) {
            throw new IllegalArgumentException("type must be an absolute URI");
        }
        if (isAboutBlank(type)) {
            throw new IllegalArgumentException("type must not be about:blank");
        }
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status must be between 100 and 599");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }

        return new ProblemType(type, status, title);
    }

    /**
     * Returns the stable machine-readable problem type URI.
     */
    public URI getType() {
        return type;
    }

    /**
     * Returns the HTTP status associated with this problem type.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the human-readable title for this problem type.
     */
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || (other instanceof ProblemType that
                && status == that.status
                && type.equals(that.type)
                && title.equals(that.title));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, status, title);
    }

    @Override
    public String toString() {
        return "ProblemType(type=" + type + ", status=" + status + ", title=" + title + ")";
    }

    private static boolean isAboutBlank(URI type) {
        return ABOUT_SCHEME.equalsIgnoreCase(type.getScheme())
            && BLANK_SCHEME_SPECIFIC_PART.equalsIgnoreCase(type.getSchemeSpecificPart());
    }
}

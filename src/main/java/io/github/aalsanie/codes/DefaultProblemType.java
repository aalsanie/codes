package io.github.aalsanie.codes;

import java.net.URI;
import java.util.Objects;

final class DefaultProblemType implements ProblemType {
    private final URI type;
    private final int status;
    private final String title;

    private DefaultProblemType(URI type, int status, String title) {
        this.type = type;
        this.status = status;
        this.title = title;
    }

    static ProblemType create(URI type, int status, String title) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(title, "title");
        if (type.toString().isEmpty()) {
            throw new IllegalArgumentException("type must not be empty");
        }
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("status must be between 100 and 599");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return new DefaultProblemType(type, status, title);
    }

    @Override
    public URI getType() {
        return type;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || (other instanceof DefaultProblemType that
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
}

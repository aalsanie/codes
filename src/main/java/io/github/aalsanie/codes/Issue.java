package io.github.aalsanie.codes;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class Issue {
    private final @Nullable OutcomeCode code;
    private final @Nullable String path;
    private final String message;

    private Issue(@Nullable OutcomeCode code, @Nullable String path, String message) {
        this.code = code;
        this.path = path;
        this.message = message;
    }

    public static Issue of(String message) {
        return create(null, null, message);
    }

    public static Issue coded(OutcomeCode code, String message) {
        return create(Objects.requireNonNull(code, "code"), null, message);
    }

    public static Issue at(String path, String message) {
        return create(null, Objects.requireNonNull(path, "path"), message);
    }

    public static Issue at(String path, OutcomeCode code, String message) {
        return create(
            Objects.requireNonNull(code, "code"),
            Objects.requireNonNull(path, "path"),
            message
        );
    }

    public @Nullable OutcomeCode getCode() {
        return code;
    }

    public @Nullable String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Issue issue)) {
            return false;
        }
        return Objects.equals(code, issue.code)
            && Objects.equals(path, issue.path)
            && message.equals(issue.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, path, message);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (code != null) {
            builder.append(code).append(' ');
        }
        if (path != null) {
            builder.append(path).append(": ");
        }
        return builder.append(message).toString();
    }

    private static Issue create(
        @Nullable OutcomeCode code,
        @Nullable String path,
        String message
    ) {
        if (path != null) {
            Constraints.requirePath(path);
        }
        Constraints.requireMessage(message);
        return new Issue(code, path, message);
    }
}

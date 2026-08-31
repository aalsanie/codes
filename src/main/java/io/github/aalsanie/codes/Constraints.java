package io.github.aalsanie.codes;

final class Constraints {
    private static final int MAX_NAMESPACE_LENGTH = 128;
    private static final int MAX_NAME_LENGTH = 64;
    private static final int MAX_MESSAGE_LENGTH = 1024;
    private static final int MAX_DETAIL_LENGTH = 4096;
    private static final int MAX_PATH_LENGTH = 256;

    private Constraints() {
    }

    static void requireNamespace(String namespace) {
        if (namespace == null) {
            throw new NullPointerException("namespace");
        }
        if (namespace.isEmpty() || namespace.length() > MAX_NAMESPACE_LENGTH) {
            throw new IllegalArgumentException(
                "namespace length must be between 1 and " + MAX_NAMESPACE_LENGTH
            );
        }

        String[] segments = namespace.split("\\.", -1);
        for (String segment : segments) {
            if (segment.isEmpty()) {
                throw new IllegalArgumentException("namespace must not contain empty segments");
            }
            if (segment.length() > 63) {
                throw new IllegalArgumentException("namespace segment length must not exceed 63");
            }
            if (!isLowerAsciiLetter(segment.charAt(0))) {
                throw new IllegalArgumentException(
                    "namespace segments must start with a lowercase ASCII letter"
                );
            }
            char last = segment.charAt(segment.length() - 1);
            if (!(isLowerAsciiLetter(last) || isAsciiDigit(last))) {
                throw new IllegalArgumentException(
                    "namespace segments must end with a lowercase ASCII letter or digit"
                );
            }
            for (int i = 0; i < segment.length(); i++) {
                char c = segment.charAt(i);
                if (!(isLowerAsciiLetter(c) || isAsciiDigit(c) || c == '-')) {
                    throw new IllegalArgumentException(
                        "namespace segments may contain only lowercase ASCII letters, digits, and hyphens"
                    );
                }
            }
        }
    }

    static void requireName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "name length must be between 1 and " + MAX_NAME_LENGTH
            );
        }
        if (!isUpperAsciiLetter(name.charAt(0))) {
            throw new IllegalArgumentException("name must start with an uppercase ASCII letter");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(isUpperAsciiLetter(c) || isAsciiDigit(c) || c == '_')) {
                throw new IllegalArgumentException(
                    "name may contain only uppercase ASCII letters, digits, and underscores"
                );
            }
        }
    }

    static void requireMessage(String message) {
        requireText(message, "message", MAX_MESSAGE_LENGTH);
    }

    static void requireDetail(String detail) {
        requireText(detail, "detail", MAX_DETAIL_LENGTH);
    }

    static void requirePath(String path) {
        requireText(path, "path", MAX_PATH_LENGTH);
    }

    private static void requireText(String value, String label, int maxLength) {
        if (value == null) {
            throw new NullPointerException(label);
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(label + " length must not exceed " + maxLength);
        }
        if (value.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(label + " must not contain NUL");
        }
    }

    private static boolean isLowerAsciiLetter(char c) {
        return c >= 'a' && c <= 'z';
    }

    private static boolean isUpperAsciiLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
}

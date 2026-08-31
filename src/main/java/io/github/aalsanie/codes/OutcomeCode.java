package io.github.aalsanie.codes;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class OutcomeCode implements Comparable<OutcomeCode> {
    private static final char DELIMITER = ':';

    private final String namespace;
    private final String name;
    private final String value;

    private OutcomeCode(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
        this.value = namespace + DELIMITER + name;
    }

    public static OutcomeCode of(String namespace, String name) {
        Constraints.requireNamespace(namespace);
        Constraints.requireName(name);
        return new OutcomeCode(namespace, name);
    }

    public static OutcomeCode parse(String value) {
        Objects.requireNonNull(value, "value");
        int delimiterIndex = value.indexOf(DELIMITER);
        if (delimiterIndex <= 0 || delimiterIndex != value.lastIndexOf(DELIMITER)) {
            throw new IllegalArgumentException("outcome code must use the format namespace:NAME");
        }
        return of(value.substring(0, delimiterIndex), value.substring(delimiterIndex + 1));
    }

    public static @Nullable OutcomeCode parseOrNull(String value) {
        Objects.requireNonNull(value, "value");
        try {
            return parse(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(OutcomeCode other) {
        return value.compareTo(Objects.requireNonNull(other, "other").value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OutcomeCode that)) {
            return false;
        }
        return namespace.equals(that.namespace) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return 31 * namespace.hashCode() + name.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}

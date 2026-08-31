package io.github.aalsanie.codes;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract sealed class MappingResult<T> permits MappingResult.Mapped, MappingResult.Unmapped {
    private MappingResult() {
    }

    public static <T> MappingResult<T> mapped(T value) {
        return new Mapped<>(Objects.requireNonNull(value, "value"));
    }

    @SuppressWarnings("unchecked")
    public static <T> MappingResult<T> unmapped() {
        return (MappingResult<T>) Unmapped.INSTANCE;
    }

    public abstract boolean isMapped();

    public final boolean isUnmapped() {
        return !isMapped();
    }

    public abstract @Nullable T orNull();

    public final <R> R fold(Function<? super T, ? extends R> onMapped, Supplier<? extends R> onUnmapped) {
        Objects.requireNonNull(onMapped, "onMapped");
        Objects.requireNonNull(onUnmapped, "onUnmapped");
        if (this instanceof Mapped<T> mapped) {
            return onMapped.apply(mapped.value);
        }
        return onUnmapped.get();
    }

    public static final class Mapped<T> extends MappingResult<T> {
        private final T value;

        private Mapped(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public boolean isMapped() {
            return true;
        }

        @Override
        public @Nullable T orNull() {
            return value;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof Mapped<?> mapped && value.equals(mapped.value));
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return "Mapped(value=" + value + ")";
        }
    }

    public static final class Unmapped<T> extends MappingResult<T> {
        private static final Unmapped<?> INSTANCE = new Unmapped<>();

        private Unmapped() {
        }

        @Override
        public boolean isMapped() {
            return false;
        }

        @Override
        public @Nullable T orNull() {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "Unmapped";
        }
    }
}

package io.github.broqlinq.jaguar.core.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

@FunctionalInterface
public interface Equivalence<T> {

    static <T, R> Equivalence<T> by(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return (a, b) -> {
            if (a == b) {
                return true;
            }

            var ra = mapper.apply(a);
            var rb = mapper.apply(b);
            return Objects.equals(ra, rb);
        };
    }

    static <T> Equivalence<T> byInt(ToIntFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return (a, b) -> {
            if (a == b) {
                return true;
            }
            var ra = mapper.applyAsInt(a);
            var rb = mapper.applyAsInt(b);
            return ra == rb;
        };
    }

    static <T> Equivalence<T> byLong(ToLongFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return (a, b) -> {
            if (a == b) {
                return true;
            }
            var ra = mapper.applyAsLong(a);
            var rb = mapper.applyAsLong(b);
            return ra == rb;
        };
    }

    static <T> Equivalence<T> byDouble(ToDoubleFunction<? super T> mapper) {
        Objects.requireNonNull(mapper);
        return (a, b) -> {
            if (a == b) {
                return true;
            }
            var ra = mapper.applyAsDouble(a);
            var rb = mapper.applyAsDouble(b);
            return ra == rb;
        };
    }

    static <T> Equivalence<T> identity() {
        return (a, b) -> a == b;
    }

    static <T> Equivalence<T> equals() {
        return Objects::equals;
    }

    static <T> Equivalence<T> neverEqual() {
        return (_, _) -> false;
    }

    static <T> Equivalence<T> alwaysEqual() {
        return (_, _) -> true;
    }

    boolean areEqual(T a, T b);

    default Equivalence<T> and(Equivalence<? super T> other) {
        Objects.requireNonNull(other);
        return (a, b) -> areEqual(a, b) && other.areEqual(a, b);
    }

    default <R> Equivalence<T> and(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return and(Equivalence.by(mapper));
    }
}

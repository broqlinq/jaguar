package io.github.broqlinq.jaguar.reactive.function;

import java.util.function.BiFunction;

@FunctionalInterface
public interface Setter<S, T> extends BiFunction<S, T, S> {

    S set(S state, T value);

    @Override
    default S apply(S s, T value) {
        return set(s, value);
    }

    @FunctionalInterface
    interface OfBoolean<S> {
        S setBoolean(S s, boolean value);
    }

    @FunctionalInterface
    interface OfInt<S> {
        S setInt(S s, int value);
    }

    @FunctionalInterface
    interface OfLong<S> {
        S setLong(S s, long value);
    }

    @FunctionalInterface
    interface OfFloat<S> {
        S setFloat(S s, float value);
    }

    @FunctionalInterface
    interface OfDouble<S> {
        S setDouble(S s, double value);
    }
}

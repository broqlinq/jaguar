package io.github.broqlinq.jaguar.reactive.function;

import java.util.function.*;

@FunctionalInterface
public interface Getter<S, T> extends Function<S, T> {

    T get(S s);

    @Override
    default T apply(S state) {
        return get(state);
    }

    @FunctionalInterface
    interface OfBoolean<S> extends Predicate<S> {
        boolean getBoolean(S s);

        @Override
        default boolean test(S s) {
            return getBoolean(s);
        }
    }

    @FunctionalInterface
    interface OfInt<S> extends ToIntFunction<S> {
        int getInt(S s);

        @Override
        default int applyAsInt(S s) {
            return getInt(s);
        }
    }

    @FunctionalInterface
    interface OfLong<S> extends ToLongFunction<S> {
        long getLong(S s);

        @Override
        default long applyAsLong(S s) {
            return getLong(s);
        }
    }

    @FunctionalInterface
    interface OfFloat<S> {
        float getFloat(S s);
    }

    @FunctionalInterface
    interface OfDouble<S> extends ToDoubleFunction<S> {
        double getDouble(S s);

        @Override
        default double applyAsDouble(S s) {
            return getDouble(s);
        }
    }
}

package io.github.broqlinq.jaguar.util;

import java.util.function.BiFunction;

/// A utility class with broad function types compatible with
/// those in `java.util.function` package and new ones.
public final class Functions {

    private Functions() {}

    @FunctionalInterface
    public interface Combiner<R, T> extends BiFunction<R, T, R> {
        R combine(R r, T t);

        @Override
        default R apply(R r, T t) {
            return combine(r, t);
        }
    }

    @FunctionalInterface
    public interface BooleanCombiner<R> {
        R combine(R r, boolean b);
    }

    @FunctionalInterface
    public interface IntCombiner<R> {
        R combine(R r, int i);
    }

    @FunctionalInterface
    public interface LongCombiner<R> {
        R combine(R r, long l);
    }

    @FunctionalInterface
    public interface FloatCombiner<R> {
        R combine(R r, float f);
    }

    @FunctionalInterface
    public interface DoubleCombiner<R> {
        R combine(R r, double d);
    }
}

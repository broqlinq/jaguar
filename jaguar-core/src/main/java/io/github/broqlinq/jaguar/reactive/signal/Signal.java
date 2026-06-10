package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.Getter;
import io.github.broqlinq.jaguar.reactive.function.ToFloatFunction;

import java.util.Objects;
import java.util.function.*;

public interface Signal<T> {

    T value();

    <R> Signal<R> compute(Function<? super T, ? extends R> fn);

    BooleanSignal compute(Predicate<? super T> fn);

    IntSignal compute(ToIntFunction<? super T> fn);

    LongSignal compute(ToLongFunction<? super T> fn);

    FloatSignal compute(ToFloatFunction<? super T> fn);

    DoubleSignal compute(ToDoubleFunction<? super T> fn);

    default <R> R extract(Getter<? super T, ? extends R> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.get(value());
    }

    default boolean extract(Getter.OfBoolean<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getBoolean(value());
    }

    default int extract(Getter.OfInt<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getInt(value());
    }

    default long extract(Getter.OfLong<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getLong(value());
    }

    default float extract(Getter.OfFloat<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getFloat(value());
    }

    default double extract(Getter.OfDouble<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getDouble(value());
    }
}

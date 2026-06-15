package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.Getter;
import io.github.broqlinq.jaguar.reactive.function.ToFloatFunction;

import java.util.Objects;
import java.util.function.*;

public interface Signal<T> {

    T value();

    <R> Signal<R> derive(Function<? super T, ? extends R> fn);

    BooleanSignal deriveBoolean(Predicate<? super T> fn);

    IntSignal deriveInt(ToIntFunction<? super T> fn);

    LongSignal deriveLong(ToLongFunction<? super T> fn);

    FloatSignal deriveFloat(ToFloatFunction<? super T> fn);

    DoubleSignal deriveDouble(ToDoubleFunction<? super T> fn);

    default <R> R extract(Getter<? super T, ? extends R> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.get(value());
    }

    default boolean extractBoolean(Getter.OfBoolean<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getBoolean(value());
    }

    default int extractInt(Getter.OfInt<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getInt(value());
    }

    default long extractLong(Getter.OfLong<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getLong(value());
    }

    default float extractFloat(Getter.OfFloat<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getFloat(value());
    }

    default double extractDouble(Getter.OfDouble<? super T> getter) {
        Objects.requireNonNull(getter, "getter");
        return getter.getDouble(value());
    }
}

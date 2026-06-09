package io.github.broqlinq.jaguar.reactive.signal;

import java.util.function.*;

public interface Signal<T> {

    T value();

    <R> Signal<R> compute(Function<? super T, ? extends R> fn);

    BooleanSignal computeBoolean(Predicate<? super T> fn);

    IntSignal computeInt(ToIntFunction<? super T> fn);

    LongSignal computeLong(ToLongFunction<? super T> fn);

    DoubleSignal computeDouble(ToDoubleFunction<? super T> fn);

    default <R> R extract(Function<? super T, ? extends R> fn) {
        return fn.apply(value());
    }

    default boolean extractBoolean(Predicate<? super T> fn) {
        return fn.test(value());
    }

    default int extractInt(ToIntFunction<? super T> fn) {
        return fn.applyAsInt(value());
    }

    default long extractLong(ToLongFunction<? super T> fn) {
        return fn.applyAsLong(value());
    }

    default double extractDouble(ToDoubleFunction<? super T> fn) {
        return fn.applyAsDouble(value());
    }
}

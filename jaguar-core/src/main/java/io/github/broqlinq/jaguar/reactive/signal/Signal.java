package io.github.broqlinq.jaguar.reactive.signal;

import java.util.function.*;

public interface Signal<T> {

    T value();

    <R> Signal<R> compute(Function<? super T, ? extends R> fn);

    BooleanSignal compute(Predicate<? super T> fn);

    IntSignal compute(ToIntFunction<? super T> fn);

    LongSignal compute(ToLongFunction<? super T> fn);

    DoubleSignal compute(ToDoubleFunction<? super T> fn);

    default <R> R extract(Function<? super T, ? extends R> fn) {
        return fn.apply(value());
    }

    default boolean extract(Predicate<? super T> fn) {
        return fn.test(value());
    }

    default int extract(ToIntFunction<? super T> fn) {
        return fn.applyAsInt(value());
    }

    default long extract(ToLongFunction<? super T> fn) {
        return fn.applyAsLong(value());
    }

    default double extract(ToDoubleFunction<? super T> fn) {
        return fn.applyAsDouble(value());
    }
}

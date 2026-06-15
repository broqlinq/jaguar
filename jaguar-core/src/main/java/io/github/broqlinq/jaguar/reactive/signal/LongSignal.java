package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.LongToFloatFunction;

import java.util.Objects;
import java.util.function.*;

public interface LongSignal {

    long value();

    <T> Signal<T> derive(LongFunction<? extends T> fn, BiPredicate<? super T, ? super T> equality);

    default <T> Signal<T> derive(LongFunction<T> fn) {
        return derive(fn, Objects::equals);
    }

    BooleanSignal deriveBoolean(LongPredicate fn);

    IntSignal deriveInt(LongToIntFunction fn);

    LongSignal deriveLong(LongUnaryOperator fn);

    FloatSignal deriveFloat(LongToFloatFunction fn);

    DoubleSignal deriveDouble(LongToDoubleFunction fn);
}

package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.*;

import java.util.Objects;
import java.util.function.BiPredicate;

public interface FloatSignal {

    float value();

    <R> Signal<R> derive(FloatFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality);

    default <R> Signal<R> derive(FloatFunction<? extends R> fn) {
        return derive(fn, Objects::equals);
    }

    BooleanSignal deriveBoolean(FloatPredicate fn);

    IntSignal deriveInt(FloatToIntFunction fn);

    LongSignal deriveLong(FloatToLongFunction fn);

    FloatSignal deriveFloat(FloatUnaryOperator fn);

    DoubleSignal deriveDouble(FloatToDoubleFunction fn);
}

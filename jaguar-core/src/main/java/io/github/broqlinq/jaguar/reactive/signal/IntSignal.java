package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.IntToFloatFunction;

import java.util.Objects;
import java.util.function.*;

public interface IntSignal {

    int value();

    <R> Signal<R> derive(IntFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality);

    default <R> Signal<R> derive(IntFunction<? extends R> fn) {
        return derive(fn, Objects::equals);
    }

    BooleanSignal deriveBoolean(IntPredicate fn);

    IntSignal deriveInt(IntUnaryOperator fn);

    LongSignal deriveLong(IntToLongFunction fn);

    FloatSignal deriveFloat(IntToFloatFunction fn);

    DoubleSignal deriveDouble(IntToDoubleFunction fn);
}

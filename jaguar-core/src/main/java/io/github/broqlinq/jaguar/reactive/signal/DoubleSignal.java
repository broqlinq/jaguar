package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.DoubleToFloatFunction;

import java.util.Objects;
import java.util.function.*;

public interface DoubleSignal {

    double value();

    <R> Signal<R> derive(DoubleFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality);

    default <R> Signal<R> derive(DoubleFunction<? extends R> fn) {
        return derive(fn, Objects::equals);
    }

    BooleanSignal deriveBoolean(DoublePredicate fn);

    IntSignal deriveInt(DoubleToIntFunction fn);

    LongSignal deriveLong(DoubleToLongFunction fn);

    FloatSignal deriveFloat(DoubleToFloatFunction fn);

    DoubleSignal deriveDouble(DoubleUnaryOperator fn);
}

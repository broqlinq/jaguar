package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.*;

import java.util.Objects;
import java.util.function.BiPredicate;

public interface BooleanSignal {

    boolean value();

    <R> Signal<R> derive(BooleanFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality);

    default <R> Signal<R> derive(BooleanFunction<? extends R> fn) {
        return derive(fn, Objects::equals);
    }

    BooleanSignal deriveBoolean(BooleanUnaryOperator fn);

    IntSignal deriveInt(BooleanToIntFunction fn);

    LongSignal deriveLong(BooleanToLongFunction fn);

    FloatSignal deriveFloat(BooleanToFloatFunction fn);

    DoubleSignal deriveDouble(BooleanToDoubleFunction fn);
}

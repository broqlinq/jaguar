package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.*;

public interface BooleanSignal {

    boolean value();

    <R> Signal<R> derive(BooleanFunction<? extends R> fn);

    BooleanSignal deriveBoolean(BooleanUnaryOperator fn);

    IntSignal deriveInt(BooleanToIntFunction fn);

    LongSignal deriveLong(BooleanToLongFunction fn);

    FloatSignal deriveFloat(BooleanToFloatFunction fn);

    DoubleSignal deriveDouble(BooleanToDoubleFunction fn);
}

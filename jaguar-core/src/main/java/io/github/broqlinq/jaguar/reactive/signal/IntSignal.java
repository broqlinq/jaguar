package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.IntToFloatFunction;

import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;

public interface IntSignal {

    int value();

    BooleanSignal deriveBoolean(IntPredicate fn);

    IntSignal deriveInt(IntUnaryOperator fn);

    LongSignal deriveLong(IntToLongFunction fn);

    FloatSignal deriveFloat(IntToFloatFunction fn);

    DoubleSignal deriveDouble(IntToDoubleFunction fn);
}

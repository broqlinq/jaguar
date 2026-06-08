package io.github.broqlinq.jaguar.reactive;

import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public interface IntSignal {

    int intValue();

    <R> Signal<R> derive(IntFunction<? extends R> mapper);

    IntSignal deriveInt(IntUnaryOperator mapper);
}

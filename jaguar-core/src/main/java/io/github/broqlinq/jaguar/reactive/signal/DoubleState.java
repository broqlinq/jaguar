package io.github.broqlinq.jaguar.reactive.signal;

import java.util.function.DoubleUnaryOperator;

public interface DoubleState extends DoubleSignal {

    void set(double newValue);

    void update(DoubleUnaryOperator updater);
}

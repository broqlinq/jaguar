package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.FloatUnaryOperator;

public interface FloatState extends FloatSignal {

    void set(float value);

    void update(FloatUnaryOperator fn);
}

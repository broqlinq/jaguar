package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.BooleanUnaryOperator;

public interface BooleanState extends BooleanSignal {

    void set(boolean value);

    void update(BooleanUnaryOperator fn);

}

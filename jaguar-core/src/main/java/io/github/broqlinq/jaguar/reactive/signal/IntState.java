package io.github.broqlinq.jaguar.reactive.signal;

import java.util.function.IntUnaryOperator;

public interface IntState extends IntSignal {

    void set(int newValue);

    void update(IntUnaryOperator fn);

}

package io.github.broqlinq.jaguar.reactive;

import java.util.function.IntUnaryOperator;

public interface IntState extends IntSignal {

    void set(int value);

    void update(IntUnaryOperator updater);
    
}

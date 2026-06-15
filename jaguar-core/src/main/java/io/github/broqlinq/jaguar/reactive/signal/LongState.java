package io.github.broqlinq.jaguar.reactive.signal;

import java.util.function.LongUnaryOperator;

public interface LongState extends LongSignal {

    void set(long newValue);

    void update(LongUnaryOperator updater);
}

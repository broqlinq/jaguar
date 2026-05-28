package io.github.broqlinq.jaguar.core.signal;

import java.util.function.Supplier;

public interface Signal<T> extends Supplier<T> {

    T value();

    @Override
    default T get() {
        return value();
    }
}

package io.github.broqlinq.jaguar.core.signal;

import io.github.broqlinq.jaguar.core.internal.signal.ComputedImpl;

import java.util.function.Supplier;

public interface Signal<T> extends Supplier<T> {

    T value();

    @Override
    default T get() {
        return value();
    }

    static <T> Signal<T> ofComputed(Supplier<T> supplier) {
        return new ComputedImpl<>(supplier);
    }
}

package io.github.broqlinq.jaguar.core.old.signal;

import io.github.broqlinq.jaguar.core.old.internal.signal.ComputedImpl;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public interface Signal<T> extends Supplier<T> {

    static <T> Signal<T> ofComputed(Supplier<T> supplier) {
        return new ComputedImpl<>(supplier);
    }

    static <T> Signal<T> ofComputed(Supplier<T> supplier, BiPredicate<? super T, ? super T> eqCheck) {
        return new ComputedImpl<>(supplier, eqCheck);
    }

    T value();

    @Override
    default T get() {
        return value();
    }
}

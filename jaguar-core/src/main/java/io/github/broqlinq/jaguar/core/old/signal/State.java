package io.github.broqlinq.jaguar.core.old.signal;

import io.github.broqlinq.jaguar.core.old.internal.signal.StateImpl;

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface State<T> extends Signal<T> {

    static <T> State<T> of(T initial) {
        return new StateImpl<>(initial);
    }

    void set(T newValue);

    void set(T newValue, BiPredicate<? super T, ? super T> eqCheck);

    void set(Supplier<? extends T> supplier);

    void set(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> eqCheck);

    void update(UnaryOperator<T> updater);

    void update(UnaryOperator<T> updater, BiPredicate<? super T, ? super T> eqCheck);
}

package io.github.broqlinq.jaguar.ui.component;

import io.github.broqlinq.jaguar.reactive.Effect;
import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.reactive.State;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public interface ComponentContext {

    Scope scope();

    default <T> Signal<T> constant(T value) {
        return scope().constant(value);
    }

    default <T> Signal<T> computed(Supplier<? extends T> supplier) {
        return scope().computed(supplier);
    }

    default <T> State<T> state(T initial, BiPredicate<? super T, ? super T> eq) {
        return scope().state(initial, eq);
    }

    default Effect effect(Runnable effect) {
        return scope().effect(effect);
    }
}

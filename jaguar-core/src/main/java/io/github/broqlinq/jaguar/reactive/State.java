package io.github.broqlinq.jaguar.reactive;

import java.util.function.UnaryOperator;

public interface State<T> extends Signal<T> {

    void set(T value);

    default void update(UnaryOperator<T> updater) {
        set(updater.apply(value()));
    }

}

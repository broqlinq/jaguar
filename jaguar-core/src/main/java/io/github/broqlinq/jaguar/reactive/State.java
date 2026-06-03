package io.github.broqlinq.jaguar.reactive;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface State<T> extends Signal<T> {

    void set(T value);

    void update(UnaryOperator<T> updater);

    void mutate(Consumer<T> mutator);

}

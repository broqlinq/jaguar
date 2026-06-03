package io.github.broqlinq.jaguar.reactive;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Signal<T> extends Supplier<T> {

    @Override
    default T get() {
        return value();
    }

    T value();

    <R> Signal<R> derive(Function<? super T, ? extends R> mapper);

    default <R> R transform(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return mapper.apply(value());
    }
}

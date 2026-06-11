package io.github.broqlinq.jaguar.reactive.function;

@FunctionalInterface
public interface BooleanFunction<T> {

    T apply(boolean value);
}

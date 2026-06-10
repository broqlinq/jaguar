package io.github.broqlinq.jaguar.reactive.function;

@FunctionalInterface
public interface ToFloatFunction<T> {

    float applyAsFloat(T t);
}

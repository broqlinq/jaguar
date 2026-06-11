package io.github.broqlinq.jaguar.reactive.function;

@FunctionalInterface
public interface BooleanToLongFunction {

    long applyAsLong(boolean value);
}

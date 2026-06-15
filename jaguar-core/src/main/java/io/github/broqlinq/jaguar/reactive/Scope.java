package io.github.broqlinq.jaguar.reactive;

public interface Scope extends ReactiveContext, AutoCloseable {

    Scope openScope(String name);

    void batch(Runnable task);

    @Override
    void close();
}

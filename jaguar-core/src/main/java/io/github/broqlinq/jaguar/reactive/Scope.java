package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.internal.ReactiveContext;

public interface Scope extends ReactiveContext, AutoCloseable {

    Scope openScope(String name);

    void batch(Runnable task);

    @Override
    void close();
}

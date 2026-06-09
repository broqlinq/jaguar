package io.github.broqlinq.jaguar.reactive;

public interface Scope extends AutoCloseable {

    @Override
    void close();
}

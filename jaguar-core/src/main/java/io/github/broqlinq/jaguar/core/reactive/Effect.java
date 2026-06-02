package io.github.broqlinq.jaguar.core.reactive;

public interface Effect extends AutoCloseable {

    void run();

    @Override
    void close();
}

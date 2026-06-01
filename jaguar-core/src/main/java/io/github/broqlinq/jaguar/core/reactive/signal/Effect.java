package io.github.broqlinq.jaguar.core.reactive.signal;

public interface Effect extends AutoCloseable {

    void run();

    @Override
    void close();
}

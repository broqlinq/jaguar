package io.github.broqlinq.jaguar.reactive.signal;

public interface Effect {

    void dispose();

    boolean isDisposed();
}

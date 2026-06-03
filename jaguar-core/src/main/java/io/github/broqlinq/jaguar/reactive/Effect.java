package io.github.broqlinq.jaguar.reactive;

public interface Effect  {

    boolean isDisposed();

    void dispose();
}

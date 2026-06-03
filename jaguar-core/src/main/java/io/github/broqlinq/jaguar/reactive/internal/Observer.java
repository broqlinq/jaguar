package io.github.broqlinq.jaguar.reactive.internal;

public interface Observer {

    void invalidate();

    void addSubscription(Disposable subscription);
}

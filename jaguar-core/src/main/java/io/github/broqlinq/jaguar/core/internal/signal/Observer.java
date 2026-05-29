package io.github.broqlinq.jaguar.core.internal.signal;

public interface Observer {

    void invalidate();

    void subscribe(Subscription subscription);

    interface Subscription {

        void unsubscribe();
    }
}

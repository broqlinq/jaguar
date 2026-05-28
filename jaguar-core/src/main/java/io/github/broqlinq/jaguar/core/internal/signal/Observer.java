package io.github.broqlinq.jaguar.core.internal.signal;

public interface Observer {

    void invalidate();

    void subscribe(Observer.Subscription subscription);

    interface Subscription {

        void unsubscribe();
    }
}

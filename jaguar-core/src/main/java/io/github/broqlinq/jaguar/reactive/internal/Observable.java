package io.github.broqlinq.jaguar.reactive.internal;

public interface Observable {

    Disposable subscribe(Observer observer);
}

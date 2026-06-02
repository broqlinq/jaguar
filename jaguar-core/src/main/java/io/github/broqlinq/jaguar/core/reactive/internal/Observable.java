package io.github.broqlinq.jaguar.core.reactive.internal;

public interface Observable {

    Disposable subscribe(Observer observer);
}

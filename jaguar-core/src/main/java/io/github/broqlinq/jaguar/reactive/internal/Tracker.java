package io.github.broqlinq.jaguar.reactive.internal;

import java.util.function.Supplier;

public interface Tracker {

    void track(Observable tracked);

    <T> T evaluate(Observer observer, Supplier<? extends T> evaluation);

    void evaluate(Observer observer, Runnable effect);
}

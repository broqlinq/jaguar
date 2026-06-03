package io.github.broqlinq.jaguar.reactive.internal;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Supplier;

final class DependencyTracker implements Tracker {

    private final ArrayDeque<Observer> observers = new ArrayDeque<>();
    private final HashSet<Observer> present = new HashSet<>();

    @Override
    public void track(Observable tracked) {
        var observer = observers.peek();

        if (observer == null) {
            return;
        }

        Disposable disposable = tracked.subscribe(observer);
        observer.addSubscription(disposable);
    }

    @Override
    public <T> T evaluate(Observer observer, Supplier<? extends T> evaluation) {
        push(observer);
        try {
            return evaluation.get();
        } finally {
            pop(observer);
        }
    }

    @Override
    public void evaluate(Observer observer, Runnable effect) {
        push(observer);
        try {
            effect.run();
        } finally {
            pop(observer);
        }
    }

    private void push(Observer observer) {
        if (!present.add(observer)) {
            throw new IllegalStateException("Circular dependency detected");
        }
        observers.push(observer);
    }

    private void pop(Observer expected) {
        var observer = observers.pop();

        if (observer != expected) {
            throw new IllegalStateException("Dependency stack corruption detected");
        }

        present.remove(observer);
    }
}

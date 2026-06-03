package io.github.broqlinq.jaguar.reactive.internal;

import java.util.LinkedHashSet;

final class ObserverSupport {

    private final ObserverList observers = new ObserverList();

    public boolean addObserver(Observer observer) {
        return observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void invalidateAll() {
        var observers = this.observers.toArray();
        for (int i = observers.length - 1; i >= 0; i--) {
            observers[i].invalidate();
        }
    }

    public void clearAll() {
        observers.clear();
    }

    private static class ObserverList {

        private final LinkedHashSet<Observer> observers = new LinkedHashSet<>();

        boolean add(Observer observer) {
            return observers.add(observer);
        }

        void remove(Observer observer) {
            observers.remove(observer);
        }

        public void clear() {
            observers.clear();
        }

        private Observer[] toArray() {
            return observers.toArray(Observer[]::new);
        }

    }
}

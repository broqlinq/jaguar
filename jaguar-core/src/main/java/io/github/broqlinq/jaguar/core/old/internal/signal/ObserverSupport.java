package io.github.broqlinq.jaguar.core.old.internal.signal;

import java.util.Set;

final class ObserverSupport {

    private ObserverSupport() {
    }

    static void invalidateAll(Set<Observer> observers) {
        for (var observer : observers) {
            observer.invalidate();
        }
    }
}

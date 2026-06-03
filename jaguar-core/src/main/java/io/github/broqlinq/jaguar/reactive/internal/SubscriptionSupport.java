package io.github.broqlinq.jaguar.reactive.internal;

import java.util.ArrayList;
import java.util.List;

final class SubscriptionSupport {

    private final List<Disposable> subscriptions = new ArrayList<>();

    public void addSubscription(Disposable disposable) {
        subscriptions.add(disposable);
    }

    public void disposeAll() {
        for (int i = subscriptions.size() - 1; i >= 0; i--) {
            subscriptions.get(i).dispose();
        }
    }

    public void clearAll() {
        subscriptions.clear();
    }
}

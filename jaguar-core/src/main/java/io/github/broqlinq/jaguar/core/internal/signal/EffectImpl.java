package io.github.broqlinq.jaguar.core.internal.signal;

import io.github.broqlinq.jaguar.core.signal.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EffectImpl implements Effect, Observer {

    private final List<Subscription> subscriptions = new ArrayList<>();
    private volatile boolean dirty = true;
    private final Runnable effect;

    public EffectImpl(Runnable effect) {
        this.effect = Objects.requireNonNull(effect, "effect must not be null");
    }

    @Override
    public void invalidate() {
        dirty = true;

        BatchManager.schedule(this);
    }

    @Override
    public void subscribe(Subscription subscription) {
        synchronized (this) {
            subscriptions.add(subscription);
        }
    }

    @Override
    public void run() {
        if (!dirty) {
            return;
        }

        synchronized (this) {
            if (dirty) {
                cleanupSubscriptions();
                try (var _ = new EvaluationSession(this)) {
                    effect.run();
                    dirty = false;
                }
            }
        }
    }

    private void cleanupSubscriptions() {
        synchronized (this) {
            for (var subscription : subscriptions) {
                subscription.unsubscribe();
            }
            subscriptions.clear();
        }
    }
}

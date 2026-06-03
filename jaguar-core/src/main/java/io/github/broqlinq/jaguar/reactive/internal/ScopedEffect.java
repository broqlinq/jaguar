package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Effect;

import java.util.Objects;

final class ScopedEffect extends ScopeOwned implements Effect, Observer {

    private final SubscriptionSupport subscriptionSupport = new SubscriptionSupport();

    private final Runnable effect;
    private boolean invalidated = true;
    private boolean disposed = false;

    ScopedEffect(ScopeContext context, Runnable effect) {
        this.effect = Objects.requireNonNull(effect, "effect");
        super(context);
    }

    void run() {
        context.batch(this::runEffect);
    }

    private void runEffect() {
        if (isDisposed()) {
            return;
        }

        subscriptionSupport.disposeAll();
        subscriptionSupport.clearAll();

        context.tracker().evaluate(this, effect);
        invalidated = false;
    }

    @Override
    public void addSubscription(Disposable subscription) {
        // null-check removed as Observable is part of internal API
        if (subscription != Disposable.NULL) {
            subscriptionSupport.addSubscription(subscription);
        }
    }

    @Override
    public void invalidate() {
        if (invalidated || isDisposed()) {
            return;
        }
        invalidated = true;
        context.schedule(this::runEffect);
    }

    @Override
    public boolean isDisposed() {
        return disposed || context.isClosed();
    }

    @Override
    public void dispose() {
        context.assertOwnerThread();

        if (disposed) {
            return;
        }

        disposed = true;
        subscriptionSupport.disposeAll();
        subscriptionSupport.clearAll();
        cleanup();
    }
}

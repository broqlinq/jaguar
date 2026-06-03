package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Signal;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

final class ScopedComputed<T> extends ScopeOwned implements Signal<T>, Observer, Observable {

    private final ObserverSupport observerSupport = new ObserverSupport();
    private final SubscriptionSupport subscriptionSupport = new SubscriptionSupport();

    private final Supplier<? extends T> supplier;
    private T cachedValue = null;

    private boolean dirty = true;
    private boolean disposed = false;

    ScopedComputed(ScopeContext context, Supplier<? extends T> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        super(context);
    }

    @Override
    public T value() {
        assertReadable("Attempted read from disposed signal");

        context.tracker().track(this);

        recomputeIfNeeded();

        return cachedValue;
    }

    private void recomputeIfNeeded() {
        if (!dirty) {
            return;
        }

        subscriptionSupport.disposeAll();
        subscriptionSupport.clearAll();

        cachedValue = context.tracker().evaluate(this, supplier);
        dirty = false;
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        assertReadable("Attempted read from disposed signal");
        return context.computed(() -> mapper.apply(value()));
    }

    @Override
    public <R> R transform(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        assertReadable("Attempted read from disposed signal");
        return mapper.apply(value());
    }

    @Override
    public void invalidate() {
        if (dirty) {
            return;
        }

        dirty = true;
        context.batch(observerSupport::invalidateAll);
    }

    @Override
    public Disposable subscribe(Observer observer) {
        // null-check removed as Observable is part of internal API
        if (observerSupport.addObserver(observer)) {
            return new Subscription(observer);
        }
        return Disposable.NULL;
    }

    @Override
    public void addSubscription(Disposable subscription) {
        // null-check removed as Observable is part of internal API
        if (subscription != Disposable.NULL) {
            subscriptionSupport.addSubscription(subscription);
        }
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        observerSupport.clearAll();
        subscriptionSupport.disposeAll();
        subscriptionSupport.clearAll();
        cleanup();
    }

    @Override
    public boolean isDisposed() {
        return disposed || context.isClosed();
    }

    private class Subscription implements Disposable {

        private final Observer observer;
        private boolean disposed = false;

        Subscription(Observer observer) {
            this.observer = observer;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            observerSupport.removeObserver(observer);
        }
    }
}

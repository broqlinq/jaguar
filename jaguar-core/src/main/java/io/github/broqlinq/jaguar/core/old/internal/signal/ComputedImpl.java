package io.github.broqlinq.jaguar.core.old.internal.signal;

import io.github.broqlinq.jaguar.core.old.signal.Signal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public final class ComputedImpl<T> implements Signal<T>, Observer {

    private static final VarHandle CACHE_HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            CACHE_HANDLE = lookup.findVarHandle(ComputedImpl.class, "cachedValue", Object.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final Set<Observer> observers = ConcurrentHashMap.newKeySet();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final Supplier<? extends T> supplier;
    private final BiPredicate<? super T, ? super T> equalityChecker;
    private volatile boolean dirty = true;
    private T cachedValue;

    public ComputedImpl(Supplier<? extends T> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
        this.equalityChecker = null;
    }

    public ComputedImpl(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> equalityChecker) {
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
        this.equalityChecker = Objects.requireNonNull(equalityChecker, "equality checker must not be null");
    }

    @Override
    public T value() {
        var observer = EvaluationContext.peek();
        if (observer != null && observers.add(observer)) {
            observer.subscribe(() -> observers.remove(observer));
        }

        if (dirty) {
            synchronized (this) {
                if (dirty) {
                    var oldValue = getCachedValue();
                    var newValue = reevaluate();

                    CACHE_HANDLE.setRelease(this, newValue);
                    dirty = false;

                    var valuesEqual = equalityChecker == null
                            ? oldValue == newValue
                            : equalityChecker.test(oldValue, newValue);

                    if (!valuesEqual) {
                        BatchManager.run(() -> ObserverSupport.invalidateAll(observers));
                    }
                }
            }
        }
        return getCachedValue();
    }

    private T reevaluate() {
        cleanupSubscriptions();
        try (var _ = new EvaluationSession(this)) {
            return supplier.get();
        }
    }

    @Override
    public void invalidate() {
        if (dirty) {
            return;
        }

        dirty = true;
        BatchManager.run(() -> ObserverSupport.invalidateAll(observers));
    }

    private void cleanupSubscriptions() {
        for (var subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
    }

    @SuppressWarnings("unchecked")
    private T getCachedValue() {
        return (T) CACHE_HANDLE.getAcquire(this);
    }

    @Override
    public void subscribe(Subscription subscription) {
        synchronized (this) {
            subscriptions.add(subscription);
        }
    }

}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.reactive.State;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

final class ScopedState<T> extends ScopeOwned implements State<T>, Observable {

    private final ObserverSupport observerSupport = new ObserverSupport();

    private final BiPredicate<? super T, ? super T> eq;
    private T value;
    private boolean disposed = false;

    ScopedState(ScopeContext context, T initialValue, BiPredicate<? super T, ? super T> eq) {
        this.value = initialValue;
        this.eq = Objects.requireNonNull(eq, "eq");
        super(context);
    }

    @Override
    public void set(T newValue) {
        assertWritable("Attempted write to disposed signal");

        if (eq.test(value, newValue)) {
            return;
        }

        this.value = newValue;
        context.batch(observerSupport::invalidateAll);
    }

    @Override
    public void update(UnaryOperator<T> updater) {
        Objects.requireNonNull(updater, "updater");
        assertWritable("Attempted write to disposed signal");

        var newValue = updater.apply(this.value);
        if (eq.test(value, newValue)) {
            return;
        }

        this.value = newValue;
        context.batch(observerSupport::invalidateAll);
    }

    @Override
    public void mutate(Consumer<T> mutator) {
        Objects.requireNonNull(mutator, "mutator");
        assertWritable("Attempted write to disposed signal");

        mutator.accept(this.value);
        context.batch(observerSupport::invalidateAll);
    }

    @Override
    public T value() {
        assertReadable("Attempted read from disposed signal");
        context.tracker().track(this);
        return value;
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
    public Disposable subscribe(Observer observer) {
        // null-check removed as Observer is part of private API
        if (observerSupport.addObserver(observer)) {
            return new Subscription(observer);
        }
        return Disposable.NULL;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        observerSupport.clearAll();
        value = null;
        cleanup();
    }

    @Override
    public boolean isDisposed() {
        return disposed || context().isClosed();
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

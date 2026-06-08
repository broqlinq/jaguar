package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Signal;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.broqlinq.jaguar.reactive.internal.Consumer.State.*;

final class ScopedComputed<T> extends ProducerConsumerNode implements Signal<T> {

    private final Runnable markStaleTask = computedLinks::markDependentsStale;
    private final Runnable markInvalidTask = computedLinks::markDependentsInvalid;

    private final Supplier<? extends T> supplier;
    private final BiPredicate<? super T, ? super T> eq;
    private T cachedValue = null;

    ScopedComputed(ThreadConfinedScope context, Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> eq) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.eq = Objects.requireNonNull(eq, "eq");
        super(context, Type.COMPUTED);
        this.state = INVALID;
    }

    @Override
    public T value() {
        assertReadable();
        return valueUnchecked();
    }

    private T valueUnchecked() {
//        scope.trackingContext().track(this);
        final var current = scope.currentConsumer;
        if (current != null) {
            var link = current.producerLinks().getOrCreate(this);
            if (link.active) {
                link.markSeen();
            } else {
                link.link(this, current);
            }
        }
        validate();
        return cachedValue;
    }

    public void validate() {
        if (state == CLEAN) {
            return;
        }
        if (state == STALE && !producerLinks.detectChanged()) {
            state = CLEAN;
            return;
        }
        recompute();
    }

    private void recompute() {
        updateEpoch();
        T newValue;
//        scope.trackingContext().push(this);
//        try {
//            newValue = supplier.get();
//        } finally {
//            scope.trackingContext().pop(this);
//        }
        if (tracking) {
            throw new IllegalStateException("Circular dependency detected");
        }

        tracking = true;
        final var currentConsumer = scope.currentConsumer;
        scope.currentConsumer = this;
        try {
            newValue = supplier.get();
        } finally {
            scope.currentConsumer = currentConsumer;
            tracking = false;
        }

        producerLinks.removeStale();

        tryReplaceValue(newValue);
    }

    private void tryReplaceValue(T newValue) {
        final T oldValue = cachedValue;
        if (eq.test(oldValue, newValue)) {
            state = CLEAN;
            return;
        }
        cachedValue = newValue;
        updateVersion();
        state = CLEAN;
        notifyDependentsInvalid();
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        assertReadable();
        return scope.computed(() -> mapper.apply(valueUnchecked()));
    }

    @Override
    public <R> R transform(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        assertReadable();
        return mapper.apply(valueUnchecked());
    }

    @Override
    public void markInvalid() {
        if (state == INVALID) {
            return;
        }
        state = INVALID;
        notifyDependentsStale();
    }

    @Override
    public void markStale() {
        if (state != CLEAN) {
            return;
        }
        state = STALE;
        notifyDependentsStale();
    }

    private void notifyDependentsInvalid() {
        if (computedLinks().nonEmpty()) {
            scope.batch(markInvalidTask);
        }
    }

    private void notifyDependentsStale() {
        if (computedLinks().nonEmpty()) {
            scope.batch(markStaleTask);
        }
    }

}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.IntSignal;
import io.github.broqlinq.jaguar.reactive.Signal;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

import static io.github.broqlinq.jaguar.reactive.internal.Consumer.State.*;

final class ScopedIntComputed extends ProducerConsumerNode implements IntSignal {

    private final Runnable batchTask = computedLinks::markDependentsInvalid;

    private final IntSupplier supplier;
    private int cachedValue;

    ScopedIntComputed(ThreadConfinedScope scope, IntSupplier supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        super(scope, Type.COMPUTED);
        this.state = INVALID;
    }

    @Override
    public int intValue() {
        assertReadable();
        return valueUnchecked();
    }

    private int valueUnchecked() {
        scope.trackingContext().track(this);
        if (state != CLEAN) {
            recompute();
        }
        return cachedValue;
    }

    private void recompute() {
        updateEpoch();
        int newValue;
        scope.trackingContext().push(this);
        try {
            newValue = supplier.getAsInt();
        } finally {
            scope.trackingContext().pop(this);
        }

        producerLinks.removeStale();

        final int oldValue = cachedValue;
        if (oldValue != newValue) {
            cachedValue = newValue;
            updateVersion();
        }

        state = CLEAN;

        batchNotifyDependents();
    }

    private void batchNotifyDependents() {
        if (computedLinks().nonEmpty()) {
            scope.batch(batchTask);
        }
    }

    @Override
    public <R> Signal<R> derive(IntFunction<? extends R> mapper) {
        assertReadable();
        Objects.requireNonNull(mapper, "mapper");
        return scope.computed(() -> mapper.apply(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(IntUnaryOperator mapper) {
        assertReadable();
        Objects.requireNonNull(mapper, "mapper");
        return scope.computedInt(() -> mapper.applyAsInt(valueUnchecked()));
    }

    @Override
    public void markInvalid() {
        if (state != CLEAN) {
            return;
        }
        state = INVALID;
        batchNotifyDependents();
    }

    @Override
    public void markStale() {
        if (state != CLEAN) {
            return;
        }
        state = STALE;
        batchNotifyDependents();
    }

    @Override
    protected void onDispose() {
        state = CLEAN;
        computedLinks.unlinkAll();
        producerLinks.unlinkAll();
        scope.removeComputed(this);
    }
}

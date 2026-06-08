package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.IntSignal;
import io.github.broqlinq.jaguar.reactive.IntState;
import io.github.broqlinq.jaguar.reactive.Signal;

import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

final class ScopedIntState extends ProducerNode implements IntState {

    private final Runnable batchTask = computedLinks::markDependentsInvalid;

    private int value;

    ScopedIntState(ThreadConfinedScope scope) {
        super(scope);
    }

    ScopedIntState(ThreadConfinedScope scope, int initial) {
        super(scope);
        this.value = initial;
    }

    @Override
    public void set(int value) {
        assertWritable();
        setUnchecked(value);
    }

    private void setUnchecked(int newValue) {
        int oldValue = this.value;
        if (oldValue == newValue) {
            return;
        }
        this.value = newValue;
        updateVersion();
        scope.batch(batchTask);
    }

    @Override
    public void update(IntUnaryOperator updater) {
        assertWritable();
        final int newValue = updater.applyAsInt(value);
        setUnchecked(newValue);
    }

    @Override
    public int intValue() {
        assertReadable();
        return valueUnchecked();
    }

    private int valueUnchecked() {
        scope.trackingContext().track(this);
        return value;
    }

    @Override
    public <R> Signal<R> derive(IntFunction<? extends R> mapper) {
        assertReadable();
        return scope.computed(() -> mapper.apply(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(IntUnaryOperator mapper) {
        assertReadable();
        return scope.computedInt(() -> mapper.applyAsInt(valueUnchecked()));
    }

    @Override
    public ConsumerList effectLinks() {
        return null;
    }
}

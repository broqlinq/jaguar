package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.DoubleToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class DoubleStateSignal extends StateSignalBase implements DoubleState {

    private double value;

    public DoubleStateSignal(ConfinedScope scope, double initial) {
        super(scope);
        this.value = initial;
    }

    @Override
    public double value() {
        assertOpen();
        return valueUnchecked();
    }

    private double valueUnchecked() {
        trackRead();
        return value;
    }

    @Override
    public void set(double newValue) {
        assertOpen();

        double oldValue = value;
        if (oldValue == newValue) {
            return;
        }

        this.value = newValue;
        commitStateChange();
    }

    @Override
    public void update(DoubleUnaryOperator updater) {
        assertOpen();
        Objects.requireNonNull(updater, "updater");

        double oldValue = value;
        double newValue = updater.applyAsDouble(value);
        if (oldValue == newValue) {
            return;
        }

        this.value = newValue;
        commitStateChange();
    }

    @Override
    public <T> Signal<T> derive(DoubleFunction<? extends T> fn, BiPredicate<? super T, ? super T> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()));
    }

    @Override
    public BooleanSignal deriveBoolean(DoublePredicate fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(DoubleToIntFunction fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(DoubleToLongFunction fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(DoubleToFloatFunction fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(DoubleUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

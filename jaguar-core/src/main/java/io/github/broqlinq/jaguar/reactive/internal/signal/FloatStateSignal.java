package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.*;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.BiPredicate;

public final class FloatStateSignal extends StateSignalBase implements FloatState {

    private float value;

    public FloatStateSignal(ConfinedScope scope, float initial) {
        super(scope);
        this.value = initial;
    }

    @Override
    public float value() {
        assertOpen();
        return valueUnchecked();
    }

    private float valueUnchecked() {
        scope.track(this);
        return value;
    }

    @Override
    public void set(float newValue) {
        assertOpen();

        float oldValue = this.value;
        if (oldValue == newValue) {
            return;
        }

        this.value = newValue;
        commitStateChange();
    }

    @Override
    public void update(FloatUnaryOperator fn) {
        assertOpen();

        float oldValue = this.value;
        float newValue = fn.applyAsFloat(value);
        if (oldValue == newValue) {
            return;
        }

        this.value = newValue;
        commitStateChange();
    }

    @Override
    public <R> Signal<R> derive(FloatFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public BooleanSignal deriveBoolean(FloatPredicate fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(FloatToIntFunction fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(FloatToLongFunction fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(FloatUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(FloatToDoubleFunction fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

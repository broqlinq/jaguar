package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.IntToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class IntStateSignal extends StateSignalBase implements IntState {

    private int value;

    public IntStateSignal(ConfinedScope scope, int initial) {
        super(scope);
        this.value = initial;
    }

    @Override
    public int value() {
        preRead();
        return value;
    }

    private int valueUnchecked() {
        scope.track(this);
        return value;
    }

    @Override
    public void set(int newValue) {
        preWrite();

        int oldValue = value;
        if (oldValue == newValue) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public void update(IntUnaryOperator fn) {
        preWrite();

        int oldValue = value;
        int newValue = fn.applyAsInt(oldValue);

        if (oldValue == newValue) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public <R> Signal<R> derive(IntFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public BooleanSignal deriveBoolean(IntPredicate fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(IntUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(IntToLongFunction fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(IntToFloatFunction fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(IntToDoubleFunction fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.*;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;

public final class BooleanStateSignal extends StateSignalBase implements BooleanState {

    private boolean value;

    BooleanStateSignal(ConfinedScope scope, boolean initial) {
        super(scope);
        this.value = initial;
    }

    @Override
    public boolean value() {
        preRead();
        return value;
    }

    private boolean valueUnchecked() {
        scope.track(this);
        return value;
    }

    @Override
    public void set(boolean newValue) {
        preWrite();

        boolean oldValue = value;
        if (oldValue == newValue) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public void update(BooleanUnaryOperator fn) {
        preWrite();

        boolean oldValue = value;
        boolean newValue = fn.applyAsBoolean(oldValue);
        if (oldValue == newValue) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public <R> Signal<R> derive(BooleanFunction<? extends R> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.computed(() -> fn.apply(valueUnchecked()));
    }

    @Override
    public BooleanSignal deriveBoolean(BooleanUnaryOperator fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.computed(() -> fn.applyAsBoolean(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(BooleanToIntFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(BooleanToLongFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(BooleanToFloatFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(BooleanToDoubleFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }

}

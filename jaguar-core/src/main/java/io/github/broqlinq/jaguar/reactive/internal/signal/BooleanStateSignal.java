package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.*;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

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
        return null;
    }

    @Override
    public BooleanSignal deriveBoolean(BooleanUnaryOperator fn) {
        return null;
    }

    @Override
    public IntSignal deriveInt(BooleanToIntFunction fn) {
        return null;
    }

    @Override
    public LongSignal deriveLong(BooleanToLongFunction fn) {
        return null;
    }

    @Override
    public FloatSignal deriveFloat(BooleanToFloatFunction fn) {
        return null;
    }

    @Override
    public DoubleSignal deriveDouble(BooleanToDoubleFunction fn) {
        return null;
    }

}

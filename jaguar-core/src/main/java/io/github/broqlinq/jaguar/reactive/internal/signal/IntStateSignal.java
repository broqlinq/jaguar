package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.IntToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;

public final class IntStateSignal extends StateSignalBase implements IntState {

    private int value;

    IntStateSignal(ConfinedScope scope, int initial) {
        super(scope);
        this.value = initial;
    }

    @Override
    public int value() {
        preRead();
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
    public BooleanSignal deriveBoolean(IntPredicate fn) {
        return null;
    }

    @Override
    public IntSignal deriveInt(IntUnaryOperator fn) {
        return null;
    }

    @Override
    public LongSignal deriveLong(IntToLongFunction fn) {
        return null;
    }

    @Override
    public FloatSignal deriveFloat(IntToFloatFunction fn) {
        return null;
    }

    @Override
    public DoubleSignal deriveDouble(IntToDoubleFunction fn) {
        return null;
    }
}

package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.*;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ScopeResource;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.function.BiPredicate;

public final class FloatConstantSignal extends ScopeResource implements FloatSignal {

    private final float value;

    public FloatConstantSignal(ConfinedScope scope, float value) {
        super(scope);
        this.value = value;
    }

    @Override
    public float value() {
        assertOpen();
        return value;
    }

    @Override
    public <R> Signal<R> derive(FloatFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.constant(fn.apply(value));
    }

    @Override
    public BooleanSignal deriveBoolean(FloatPredicate fn) {
        return scope.constant(fn.test(value));
    }

    @Override
    public IntSignal deriveInt(FloatToIntFunction fn) {
        return scope.constant(fn.applyAsInt(value));
    }

    @Override
    public LongSignal deriveLong(FloatToLongFunction fn) {
        return scope.constant(fn.applyAsLong(value));
    }

    @Override
    public FloatSignal deriveFloat(FloatUnaryOperator fn) {
        return scope.constant(fn.applyAsFloat(value));
    }

    @Override
    public DoubleSignal deriveDouble(FloatToDoubleFunction fn) {
        return scope.constant(fn.applyAsDouble(value));
    }
}

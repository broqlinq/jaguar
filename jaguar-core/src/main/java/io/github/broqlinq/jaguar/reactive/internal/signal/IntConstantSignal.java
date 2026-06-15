package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.IntToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ScopeResource;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class IntConstantSignal extends ScopeResource implements IntSignal {

    private final int value;

    public IntConstantSignal(ConfinedScope scope, int value) {
        super(scope);
        this.value = value;
    }

    @Override
    public int value() {
        scope.assertOpen();
        return value;
    }

    @Override
    public <R> Signal<R> derive(IntFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.constant(fn.apply(value));
    }

    @Override
    public BooleanSignal deriveBoolean(IntPredicate fn) {
        return scope.constant(fn.test(value));
    }

    @Override
    public IntSignal deriveInt(IntUnaryOperator fn) {
        return scope.constant(fn.applyAsInt(value));
    }

    @Override
    public LongSignal deriveLong(IntToLongFunction fn) {
        return scope.constant(fn.applyAsLong(value));
    }

    @Override
    public FloatSignal deriveFloat(IntToFloatFunction fn) {
        return scope.constant(fn.applyAsFloat(value));
    }

    @Override
    public DoubleSignal deriveDouble(IntToDoubleFunction fn) {
        return scope.constant(fn.applyAsDouble(value));
    }
}

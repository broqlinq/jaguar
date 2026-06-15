package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.DoubleToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ScopeResource;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.function.*;

public final class DoubleConstantSignal extends ScopeResource implements DoubleSignal {

    private final double value;

    public DoubleConstantSignal(ConfinedScope scope, double value) {
        super(scope);
        this.value = value;
    }

    @Override
    public double value() {
        assertOpen();
        return value;
    }

    @Override
    public <T> Signal<T> derive(DoubleFunction<? extends T> fn, BiPredicate<? super T, ? super T> equality) {
        return scope.constant(fn.apply(value));
    }

    @Override
    public BooleanSignal deriveBoolean(DoublePredicate fn) {
        return scope.constant(fn.test(value));
    }

    @Override
    public IntSignal deriveInt(DoubleToIntFunction fn) {
        return scope.constant(fn.applyAsInt(value));
    }

    @Override
    public LongSignal deriveLong(DoubleToLongFunction fn) {
        return scope.constant(fn.applyAsLong(value));
    }

    @Override
    public FloatSignal deriveFloat(DoubleToFloatFunction fn) {
        return scope.constant(fn.applyAsFloat(value));
    }

    @Override
    public DoubleSignal deriveDouble(DoubleUnaryOperator fn) {
        return scope.constant(fn.applyAsDouble(value));
    }
}

package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.*;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ScopeResource;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;

public class BooleanConstantSignal extends ScopeResource implements BooleanSignal {

    private final boolean value;

    public BooleanConstantSignal(ConfinedScope scope, boolean value) {
        super(scope);
        this.value = value;
    }

    @Override
    public boolean value() {
        scope.assertOpen();
        return value;
    }

    @Override
    public <R> Signal<R> derive(BooleanFunction<? extends R> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.apply(value));
    }

    @Override
    public BooleanSignal deriveBoolean(BooleanUnaryOperator fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsBoolean(value));
    }

    @Override
    public IntSignal deriveInt(BooleanToIntFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsInt(value));
    }

    @Override
    public LongSignal deriveLong(BooleanToLongFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsLong(value));
    }

    @Override
    public FloatSignal deriveFloat(BooleanToFloatFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsFloat(value));
    }

    @Override
    public DoubleSignal deriveDouble(BooleanToDoubleFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsDouble(value));
    }
}

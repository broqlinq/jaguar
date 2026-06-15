package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.LongToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ScopeResource;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class LongConstantSignal extends ScopeResource implements LongSignal {

    private final long value;

    public LongConstantSignal(ConfinedScope scope, long value) {
        super(scope);
        this.value = value;
    }

    @Override
    public long value() {
        assertOpen();
        return value;
    }

    @Override
    public <T> Signal<T> derive(LongFunction<? extends T> fn, BiPredicate<? super T, ? super T> equality) {
        Objects.requireNonNull(fn, "fn");
        Objects.requireNonNull(equality, "equality");
        return scope.constant(fn.apply(value));
    }

    @Override
    public BooleanSignal deriveBoolean(LongPredicate fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.test(value));
    }

    @Override
    public IntSignal deriveInt(LongToIntFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsInt(value));
    }

    @Override
    public LongSignal deriveLong(LongUnaryOperator fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsLong(value));
    }

    @Override
    public FloatSignal deriveFloat(LongToFloatFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsFloat(value));
    }

    @Override
    public DoubleSignal deriveDouble(LongToDoubleFunction fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsDouble(value));
    }
}

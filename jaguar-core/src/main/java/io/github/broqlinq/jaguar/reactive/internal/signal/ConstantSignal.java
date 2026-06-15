package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.ToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ScopeResource;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class ConstantSignal<T> extends ScopeResource implements Signal<T> {

    private final T value;

    public ConstantSignal(ConfinedScope scope, T value) {
        super(scope);
        this.value = value;
    }

    @Override
    public T value() {
        scope.assertOpen();
        return value;
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.apply(value));
    }

    @Override
    public BooleanSignal deriveBoolean(Predicate<? super T> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.test(value));
    }

    @Override
    public IntSignal deriveInt(ToIntFunction<? super T> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsInt(value));
    }

    @Override
    public LongSignal deriveLong(ToLongFunction<? super T> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsLong(value));
    }

    @Override
    public FloatSignal deriveFloat(ToFloatFunction<? super T> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsFloat(value));
    }

    @Override
    public DoubleSignal deriveDouble(ToDoubleFunction<? super T> fn) {
        Objects.requireNonNull(fn, "fn");
        return scope.constant(fn.applyAsDouble(value));
    }

}

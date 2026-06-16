package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.ReactiveContext;
import io.github.broqlinq.jaguar.reactive.function.FloatSupplier;
import io.github.broqlinq.jaguar.reactive.internal.signal.*;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

final class ReactiveContextImpl implements ReactiveContext {

    private final ConfinedScope scope;

    public ReactiveContextImpl(ConfinedScope scope) {
        this.scope = scope;
    }

    @Override
    public <T> ConstantSignal<T> constant(T value) {
        return new ConstantSignal<>(scope, value);
    }

    @Override
    public BooleanSignal constant(boolean value) {
        return new BooleanConstantSignal(scope, value);
    }

    @Override
    public IntSignal constant(int value) {
        return new IntConstantSignal(scope, value);
    }

    @Override
    public LongSignal constant(long value) {
        return new LongConstantSignal(scope, value);
    }

    @Override
    public FloatSignal constant(float value) {
        return new FloatConstantSignal(scope, value);
    }

    @Override
    public DoubleSignal constant(double value) {
        return new DoubleConstantSignal(scope, value);
    }

    @Override
    public <T> StateSignal<T> state(T initial, BiPredicate<? super T, ? super T> equality) {
        Objects.requireNonNull(equality, "equality");
        return new StateSignal<>(scope, initial, equality);
    }

    @Override
    public BooleanStateSignal state(boolean initial) {
        return new BooleanStateSignal(scope, initial);
    }

    @Override
    public IntStateSignal state(int initial) {
        return new IntStateSignal(scope, initial);
    }

    @Override
    public LongStateSignal state(long initial) {
        return new LongStateSignal(scope, initial);
    }

    @Override
    public FloatStateSignal state(float initial) {
        return new FloatStateSignal(scope, initial);
    }

    @Override
    public DoubleStateSignal state(double initial) {
        return new DoubleStateSignal(scope, initial);
    }

    @Override
    public <T> ComputedSignal<T> computed(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> equality) {
        Objects.requireNonNull(supplier, "supplier");
        return new ComputedSignal<>(scope, supplier, equality);
    }

    @Override
    public BooleanComputedSignal computed(BooleanSupplier supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return new BooleanComputedSignal(scope, supplier);
    }

    @Override
    public IntComputedSignal computed(IntSupplier supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return new IntComputedSignal(scope, supplier);
    }

    @Override
    public LongComputedSignal computed(LongSupplier supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return new LongComputedSignal(scope, supplier);
    }

    @Override
    public FloatComputedSignal computed(FloatSupplier supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return new FloatComputedSignal(scope, supplier);
    }

    @Override
    public DoubleComputedSignal computed(DoubleSupplier supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return new DoubleComputedSignal(scope, supplier);
    }

    @Override
    public ReactiveEffect effect(Runnable effect) {
        Objects.requireNonNull(effect, "effect");
        return new ReactiveEffect(scope, effect);
    }
}

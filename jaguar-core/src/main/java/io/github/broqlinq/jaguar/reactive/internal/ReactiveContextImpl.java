package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.function.FloatSupplier;
import io.github.broqlinq.jaguar.reactive.internal.signal.*;
import io.github.broqlinq.jaguar.reactive.signal.*;

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
        return null;
    }

    @Override
    public IntSignal constant(int value) {
        return null;
    }

    @Override
    public LongSignal constant(long value) {
        return null;
    }

    @Override
    public FloatSignal constant(float value) {
        return null;
    }

    @Override
    public DoubleSignal constant(double value) {
        return null;
    }

    @Override
    public <T> StateSignal<T> state(T initial, BiPredicate<? super T, ? super T> predicate) {
        return new StateSignal<>(scope, initial, predicate);
    }

    @Override
    public BooleanStateSignal state(boolean initial) {
        return null;
    }

    @Override
    public IntStateSignal state(int initial) {
        return null;
    }

    @Override
    public LongState state(long initial) {
        return null;
    }

    @Override
    public FloatState state(float initial) {
        return null;
    }

    @Override
    public DoubleState state(double initial) {
        return null;
    }

    @Override
    public <T> ComputedSignal<T> computed(Supplier<? extends T> calc, BiPredicate<? super T, ? super T> predicate) {
        return new ComputedSignal<>(scope, calc, predicate);
    }

    @Override
    public BooleanSignal computed(BooleanSupplier calc) {
        return null;
    }

    @Override
    public IntSignal computed(IntSupplier calc) {
        return null;
    }

    @Override
    public LongSignal computed(LongSupplier calc) {
        return null;
    }

    @Override
    public FloatSignal computed(FloatSupplier calc) {
        return null;
    }

    @Override
    public DoubleSignal computed(DoubleSupplier calc) {
        return null;
    }

    @Override
    public ReactiveEffect effect(Runnable effect) {
        return new ReactiveEffect(scope, effect);
    }
}

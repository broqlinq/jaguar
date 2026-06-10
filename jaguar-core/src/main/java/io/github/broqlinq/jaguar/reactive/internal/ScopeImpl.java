package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.function.*;

public final class ScopeImpl implements Scope {

    private final SchedulerImpl scheduler;

    public ScopeImpl(SchedulerImpl scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public <T> Signal<T> constant(T value) {
        return null;
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
    public DoubleSignal constant(double value) {
        return null;
    }

    @Override
    public <T> State<T> state(T initial, BiPredicate<? super T, ? super T> predicate) {
        return null;
    }

    @Override
    public BooleanState state(boolean initial) {
        return null;
    }

    @Override
    public IntState state(int initial) {
        return null;
    }

    @Override
    public LongState state(long initial) {
        return null;
    }

    @Override
    public DoubleState state(double initial) {
        return null;
    }

    @Override
    public <T> Signal<T> computed(Supplier<? extends T> calc) {
        return null;
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
    public DoubleSignal computed(DoubleSupplier calc) {
        return null;
    }

    @Override
    public Effect effect(Runnable effect) {
        return null;
    }

    @Override
    public void schedule(Runnable task) {

    }

    @Override
    public void batch(Runnable task) {

    }

    @Override
    public void close() {

    }
}

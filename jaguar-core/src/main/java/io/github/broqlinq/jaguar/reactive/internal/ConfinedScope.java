package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.function.FloatSupplier;
import io.github.broqlinq.jaguar.reactive.signal.IntSignal;
import io.github.broqlinq.jaguar.reactive.signal.IntState;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class ConfinedScope implements Scope, TrackingContext {

    private final SchedulerImpl scheduler;
    private final ScopeSession session = new ScopeSession();
    private final ReactiveContextImpl signalFactory = new ReactiveContextImpl(this);

    ReactiveNode tracked;

    private boolean closed;

    public ConfinedScope(SchedulerImpl scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public <T> Signal<T> constant(T value) {
        assertOwnerThread();
        assertOpen();
        return signalFactory.constant(value);
    }

    @Override
    public BooleanSignal constant(boolean value) {
        assertOwnerThread();
        assertOpen();
        return signalFactory.constant(value);
    }

    @Override
    public IntSignal constant(int value) {
        assertOwnerThread();
        assertOpen();
        return signalFactory.constant(value);
    }

    @Override
    public LongSignal constant(long value) {
        assertOwnerThread();
        assertOpen();
        return signalFactory.constant(value);
    }

    @Override
    public FloatSignal constant(float value) {
        assertOwnerThread();
        assertOpen();
        return signalFactory.constant(value);
    }

    @Override
    public DoubleSignal constant(double value) {
        assertOwnerThread();
        assertOpen();
        return signalFactory.constant(value);
    }

    @Override
    public <T> State<T> state(T initial, BiPredicate<? super T, ? super T> predicate) {
        assertOwnerThread();
        assertOpen();
        var state = signalFactory.state(initial, predicate);
        session.addStateResource(state);
        return state;
    }

    @Override
    public BooleanState state(boolean initial) {
        assertOwnerThread();
        assertOpen();
        var state = signalFactory.state(initial);
        session.addStateResource(state);
        return state;
    }

    @Override
    public IntState state(int initial) {
        assertOwnerThread();
        assertOpen();
        var state = signalFactory.state(initial);
        session.addStateResource(state);
        return state;
    }

    @Override
    public LongState state(long initial) {
        assertOwnerThread();
        assertOpen();
        var state = signalFactory.state(initial);
        session.addStateResource(state);
        return state;
    }

    @Override
    public FloatState state(float initial) {
        assertOwnerThread();
        assertOpen();
        var state = signalFactory.state(initial);
        session.addStateResource(state);
        return state;
    }

    @Override
    public DoubleState state(double initial) {
        assertOwnerThread();
        assertOpen();
        var state = signalFactory.state(initial);
        session.addStateResource(state);
        return state;
    }

    @Override
    public <T> Signal<T> computed(Supplier<? extends T> fn, BiPredicate<? super T, ? super T> equality) {
        assertOwnerThread();
        assertOpen();

        Objects.requireNonNull(fn, "fn");
        Objects.requireNonNull(equality, "equality");

        var computed = signalFactory.<T>computed(fn, equality);
        session.addComputedResource(computed);
        return computed;
    }

    @Override
    public BooleanSignal computed(BooleanSupplier calc) {
        assertOwnerThread();
        assertOpen();
        return null;
    }

    @Override
    public IntSignal computed(IntSupplier calc) {
        assertOwnerThread();
        assertOpen();
        return null;
    }

    @Override
    public LongSignal computed(LongSupplier calc) {
        assertOwnerThread();
        assertOpen();
        return null;
    }

    @Override
    public FloatSignal computed(FloatSupplier calc) {
        assertOwnerThread();
        assertOpen();
        return null;
    }

    @Override
    public DoubleSignal computed(DoubleSupplier calc) {
        assertOwnerThread();
        assertOpen();
        return null;
    }

    @Override
    public Effect effect(Runnable effect) {
        assertOwnerThread();
        assertOpen();
        var eff = signalFactory.effect(effect);
        session.addEffectResource(eff);
        eff.schedule();
        return eff;
    }

    private void schedule(Runnable task) {
        assertOwnerThread();
        scheduler.schedule(task);
    }

    public void scheduleUnchecked(Runnable task) {
        scheduler.scheduleUnchecked(task);
    }

    @Override
    public Scope openScope(String name) {
        assertOwnerThread();
        assertOpen();
        // TODO: make scope extend resource and have parent reference
        return new ConfinedScope(scheduler);
    }

    @Override
    public void batch(Runnable task) {
        assertOwnerThread();
        scheduler.batch(task);
    }

    void batchUnchecked(Runnable task) {
        scheduler.batchUnchecked(task);
    }

    @Override
    public void close() {
        assertOwnerThread();
        closeUnchecked();
    }

    void closeUnchecked() {
        if (closed) {
            return;
        }
        closed = true;
        var accumulator = new ExceptionAccumulator();
        accumulator.runTask(session::close)
                .throwIfFailed();
    }

    public boolean isClosed() {
        return closed;
    }

    public void assertOwnerThread() {
        scheduler.assertOwnerThread();
    }

    public void assertOpen() {
        scheduler.assertOwnerThread();
        if (isClosed()) {
            throw new IllegalStateException("Scope already closed");
        }
    }

    @Override
    public ReactiveNode current() {
        return tracked;
    }

    @Override
    public ReactiveNode push(ReactiveNode node) {
        var previous = tracked;
        tracked = node;
        return previous;
    }

    @Override
    public void pop(ReactiveNode previous) {
        tracked = previous;
    }
}

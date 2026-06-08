package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class ThreadConfinedScope implements Scope {

    private final String name;

    private final Scope parent;

    private final ArrayList<Scope> children = new ArrayList<>();

    private final Scheduler scheduler;

    private final TrackingContext tracker = new TrackingContext();

    private final LinkedHashSet<Scoped> effects = new LinkedHashSet<>();
    private final LinkedHashSet<Scoped> computedSignals = new LinkedHashSet<>();
    private final LinkedHashSet<Scoped> stateSignals = new LinkedHashSet<>();
    private boolean closed = false;
    private boolean closing = false;

    Consumer currentConsumer;

    public ThreadConfinedScope(String name, Scheduler scheduler) {
        this(name, null, scheduler);
    }

    public ThreadConfinedScope(String name, Scope parent, Scheduler scheduler) {
        this.name = normalize(name);
        this.parent = parent;
        this.scheduler = scheduler;
    }

    private static String normalize(String name) {
        if (name == null || name.isBlank()) {
            return "<unnamed-scope>";
        }
        return name;
    }

    private static <T> T[] snapshotArray(Collection<? extends T> collection, IntFunction<T[]> arraySupplier) {
        return collection.toArray(arraySupplier);
    }

    public static Scope create(String name) {
        return new ThreadConfinedScope(name, new ThreadConfinedScheduler(Thread.currentThread()));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Scope child(String name) {
        assertAccess();
        var scope = new ThreadConfinedScope(name, this, scheduler);
        children.add(scope);
        return scope;
    }

    @Override
    public Scheduler scheduler() {
        assertAccess();
        return scheduler;
    }

    @Override
    public <T> Signal<T> constant(T value) {
        assertAccess();

        return new ScopedConstant<>(this, value);
    }

    @Override
    public IntSignal intConstant(int value) {
        assertAccess();
        return new ScopedIntConstant(this, value);
    }

    @Override
    public <T> Signal<T> computed(Supplier<? extends T> supplier) {
        assertAccess();

        var computed = new ScopedComputed<T>(this, supplier, Objects::equals);
        computedSignals.add(computed);
        return computed;
    }

    @Override
    public <T> Signal<T> computed(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> eq) {
        // Ignore for now, return regular computed signal
        return computed(supplier);
    }

    @Override
    public IntSignal computedInt(IntSupplier supplier) {
        assertAccess();
        var computedInt = new ScopedIntComputed(this, supplier);
        computedSignals.add(computedInt);
        return computedInt;
    }

    @Override
    public <T> State<T> state(T initial, BiPredicate<? super T, ? super T> eq) {
        assertAccess();
        var state = new ScopedState<>(this, initial, eq);
        stateSignals.add(state);
        return state;
    }

    @Override
    public IntState intState(int initial) {
        assertAccess();
        var intState = new ScopedIntState(this, initial);
        stateSignals.add(intState);
        return intState;
    }

    @Override
    public Effect effect(Runnable eff) {
        assertAccess();
        var effect = new ScopedEffect(this, eff);
        effects.add(effect);
        effect.scheduleEffect();
        return effect;
    }

    @Override
    public boolean isClosed() {
        var closed = this.closed;
        if (parent != null) {
            closed |= parent.isClosed();
        }
        return closed;
    }

    TrackingContext trackingContext() {
        assertOpen();
        return tracker;
    }

    public void assertOwnerThread() {
        if (!scheduler.isOwnerThread()) {
            throw new WrongThreadException("Attempted access from non-parent thread in scope '" + name + "'");
        }
    }

    @Override
    public void batch(Runnable task) {
        assertOpen();
        scheduler.batch(task);
    }

    @Override
    public void schedule(Runnable task) {
        assertOpen();
        scheduler.schedule(task);
    }

    private void assertOpen() {
        if (isClosed()) {
            throw new IllegalStateException("Attempted to access closed scope '" + name + "'");
        }
    }

    private void assertAccess() {
        assertOwnerThread();
        assertOpen();
    }

    @Override
    public void close() {
        assertOwnerThread();

        if (closed || closing) {
            return;
        }

        closing = true;

        try {
            closeChildren();
            disposeEffects();
            disposeComputedSignals();
            disposeStateSignals();
            if (parent != null) {
                ((ThreadConfinedScope) parent).children.remove(this);
            }
            closed = true;
        } finally {
            closing = false;
        }
    }

    void removeEffect(ReactiveNode effect) {
        effects.remove(effect);
    }

    void removeComputed(ScopedComputed<?> computed) {
        computedSignals.remove(computed);
    }

    void removeState(ReactiveNode state) {
        stateSignals.remove(state);
    }

    private void closeChildren() {
        var children = snapshotArray(this.children.reversed(), Scope[]::new);
        for (var child : children) {
            child.close();
        }
    }

    void removeComputed(ReactiveNode computed) {
        computedSignals.remove(computed);
    }

    private void disposeEffects() {
        var effects = snapshotArray(this.effects.reversed(), Scoped[]::new);
        for (var effect : effects) {
            effect.dispose();
        }
    }

    private void disposeComputedSignals() {
        var computed = snapshotArray(this.computedSignals.reversed(), Scoped[]::new);
        for (var comp : computed) {
            comp.dispose();
        }
    }

    private void disposeStateSignals() {
        var states = snapshotArray(this.stateSignals, Scoped[]::new);
        for (var state : states) {
            state.dispose();
        }
    }
}

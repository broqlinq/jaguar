package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class ThreadConfinedScope implements Scope, ScopeContext {

    private final String name;

    private final Scope parent;

    private final ArrayList<Scope> children = new ArrayList<>();

    private final Scheduler scheduler;

    private final DependencyTracker tracker = new DependencyTracker();

    private final LinkedHashSet<Scoped> effects = new LinkedHashSet<>();
    private final LinkedHashSet<Scoped> computedSignals = new LinkedHashSet<>();
    private final LinkedHashSet<Scoped> stateSignals = new LinkedHashSet<>();

    private boolean closed = false;
    private boolean closing = false;

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
    public <T> Signal<T> computed(Supplier<? extends T> supplier) {
        assertAccess();

        var computed = new ScopedComputed<T>(this, supplier);
        computedSignals.add(computed);
        computed.onDispose(() -> computedSignals.remove(computed));
        return computed;
    }

    @Override
    public <T> Signal<T> computed(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> eq) {
        // Ignore for now
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <T> State<T> state(T initial, BiPredicate<? super T, ? super T> eq) {
        assertAccess();

        var state = new ScopedState<>(this, initial, eq);
        stateSignals.add(state);
        state.onDispose(() -> stateSignals.remove(state));
        return state;
    }

    @Override
    public Effect effect(Runnable eff) {
        assertAccess();

        var effect = new ScopedEffect(this, eff);
        effects.add(effect);
        effect.onDispose(() -> effects.remove(effect));
        effect.run();
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

    @Override
    public Tracker tracker() {
        assertOpen();
        return tracker;
    }

    @Override
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
            closed = true;
        } finally {
            closing = true;
        }
    }

    private void closeChildren() {
        var children = snapshotArray(this.children.reversed(), Scope[]::new);
        for (int i = children.length - 1; i >= 0; i--) {
            children[i].close();
        }
    }

    private void disposeEffects() {
        var effects = snapshotArray(this.effects.reversed(), Scoped[]::new);
        for (int i = effects.length - 1; i >= 0; i--) {
            effects[i].dispose();
        }
    }

    private void disposeComputedSignals() {
        var computed = snapshotArray(this.computedSignals.reversed(), Scoped[]::new);
        for (int i = computed.length - 1; i >= 0; i--) {
            computed[i].dispose();
        }
    }

    private void disposeStateSignals() {
        var states =  snapshotArray(this.stateSignals, Scoped[]::new);
        for (int i = states.length - 1; i >= 0; i--) {
            states[i].dispose();
        }
    }

}

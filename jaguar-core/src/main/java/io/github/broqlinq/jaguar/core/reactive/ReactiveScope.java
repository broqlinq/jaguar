package io.github.broqlinq.jaguar.core.reactive;

import io.github.broqlinq.jaguar.core.util.Equivalence;

import java.util.function.Supplier;

public interface ReactiveScope extends AutoCloseable {

    ReactiveScope childScope(String name);

    Scheduler scheduler();

    <T> Signal<T> constant(T value);

    <T> State<T> state(T value, Equivalence<? super T> eq);

    default <T> State<T> state(T value) {
        return state(value, Equivalence.equals());
    }

    <T> Signal<T> computed(Supplier<? extends T> supplier, Equivalence<? super T> eq);

    default <T> Signal<T> computed(Supplier<? extends T> supplier) {
        return computed(supplier, Equivalence.equals());
    }

    Effect effect(Runnable effect);

    boolean isClosed();

    void dispose();

    void onDispose(Runnable cleanup);

    @Override
    default void close() {
        dispose();
    }

    interface Scheduler {

        boolean isOwnerThread();

        void schedule(Runnable task);

        void batch(Runnable batchTask);
    }
}

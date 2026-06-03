package io.github.broqlinq.jaguar.reactive;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public interface Scope extends AutoCloseable {

    String name();

    Scope child(String name);

    Scheduler scheduler();

    <T> Signal<T> constant(T value);

    <T> Signal<T> computed(Supplier<? extends T> supplier);

    <T> Signal<T> computed(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> eq);

    <T> State<T> state(T initial, BiPredicate<? super T, ? super T> eq);

    Effect effect(Runnable eff);

    boolean isClosed();

    default void schedule(Runnable task) {
        scheduler().schedule(task);
    }

    default void batch(Runnable task) {
        scheduler().batch(task);
    }

    @Override
    void close();
}

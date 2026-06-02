package io.github.broqlinq.jaguar.core.reactive;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public interface Scope extends AutoCloseable {

    String name();

    Scope child(String name);

    Scheduler scheduler();

    <T> Signal<T> constant(T value);

    <T> Signal<T> computed(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> eq);

    <T> State<T> state(T initial, BiPredicate<? super T, ? super T> eq);

    Effect effect(Runnable eff);

    boolean isClosed();

    @Override
    void close();
}

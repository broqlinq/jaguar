package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Effect;
import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.reactive.State;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public interface ScopeContext {

    <T> Signal<T> constant(T value);

    <T> Signal<T> computed(Supplier<? extends T> supplier);

    <T> State<T> state(T initialValue, BiPredicate<? super T, ? super T> eq);

    Effect effect(Runnable eff);

    boolean isClosed();

    void batch(Runnable batch);

    void schedule(Runnable task);

    Tracker tracker();

    void assertOwnerThread();
}

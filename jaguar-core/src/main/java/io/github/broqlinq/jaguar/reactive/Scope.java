package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.function.FloatSupplier;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.function.*;

public interface Scope extends AutoCloseable {

    <T> Signal<T> constant(T value);
    BooleanSignal constant(boolean value);
    IntSignal constant(int value);
    LongSignal constant(long value);
    FloatSignal constant(float value);
    DoubleSignal constant(double value);

    <T> State<T> state(T initial, BiPredicate<? super T, ? super T> predicate);
    BooleanState state(boolean initial);
    IntState state(int initial);
    LongState state(long initial);
    FloatState state(float initial);
    DoubleState state(double initial);

    <T> Signal<T> computed(Supplier<? extends T> calc);
    BooleanSignal computed(BooleanSupplier calc);
    IntSignal computed(IntSupplier calc);
    LongSignal computed(LongSupplier calc);
    FloatSignal computed(FloatSupplier calc);
    DoubleSignal computed(DoubleSupplier calc);

    Effect effect(Runnable effect);

    void schedule(Runnable task);
    void batch(Runnable task);

    @Override
    void close();
}

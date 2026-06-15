package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.function.FloatSupplier;
import io.github.broqlinq.jaguar.reactive.signal.IntSignal;
import io.github.broqlinq.jaguar.reactive.signal.IntState;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public interface ReactiveContext {

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

    <T> Signal<T> computed(Supplier<? extends T> calc, BiPredicate<? super T, ? super T> predicate);
    default <T> Signal<T> computed(Supplier<? extends T> calc) {
        return computed(calc, Objects::equals);
    }
    BooleanSignal computed(BooleanSupplier calc);
    IntSignal computed(IntSupplier calc);
    LongSignal computed(LongSupplier calc);
    FloatSignal computed(FloatSupplier calc);
    DoubleSignal computed(DoubleSupplier calc);

    Effect effect(Runnable effect);
}

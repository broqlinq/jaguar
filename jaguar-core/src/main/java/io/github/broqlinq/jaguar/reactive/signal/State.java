package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.util.Functions.*;

import java.util.function.*;

public interface State<T> extends Signal<T> {

    void set(T newValue);

    void update(UnaryOperator<T> fn);

    <R> State<R> project(Function<? super T, ? extends R> fn, Combiner<T, R> combiner);

    BooleanState project(Predicate<? super T> fn, BooleanCombiner<T> combiner);

    IntState project(ToIntFunction<? super T> fn, IntCombiner<T> combiner);

    LongState project(ToLongFunction<? super T> fn, LongCombiner<T> combiner);

    DoubleState project(ToDoubleFunction<? super T> fn, DoubleCombiner<T> combiner);
}

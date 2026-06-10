package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.Getter;
import io.github.broqlinq.jaguar.reactive.function.Setter;

import java.util.function.UnaryOperator;

public interface State<T> extends Signal<T> {

    void set(T newValue);

    void update(UnaryOperator<T> fn);

    <R> State<R> project(Lens<T, R> lens);

    default <R> State<R> project(Getter<? super T, ? extends R> getter, Setter<T, ? super R> setter) {
        var lens = Lens.<T, R>of(getter, setter);
        return project(lens);
    }

    BooleanState project(Lens.OfBoolean<T> lens);

    default BooleanState project(Getter.OfBoolean<? super T> getter, Setter.OfBoolean<T> setter) {
        var lens = Lens.of(getter, setter);
        return project(lens);
    }

    IntState project(Lens.OfInt<T> lens);

    default IntState project(Getter.OfInt<? super T> getter, Setter.OfInt<T> setter) {
        var lens = Lens.of(getter, setter);
        return project(lens);
    }

    LongState project(Lens.OfLong<T> lens);

    default LongState project(Getter.OfLong<? super T> getter, Setter.OfLong<T> setter) {
        var lens = Lens.of(getter, setter);
        return project(lens);
    }

    FloatState project(Lens.OfFloat<T> lens);

    default FloatState project(Getter.OfFloat<? super T> getter, Setter.OfFloat<T> setter) {
        var lens = Lens.of(getter, setter);
        return project(lens);
    }

    DoubleState project(Lens.OfDouble<T> lens);

    default DoubleState project(Getter.OfDouble<? super T> getter, Setter.OfDouble<T> setter) {
        return project(Lens.of(getter, setter));
    }
}

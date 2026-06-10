package io.github.broqlinq.jaguar.reactive.signal;

import io.github.broqlinq.jaguar.reactive.function.Getter;
import io.github.broqlinq.jaguar.reactive.function.Setter;

public interface Lens<S, T> {

    static <S, T> Lens<S, T> of(
            Getter<? super S, ? extends T> getter,
            Setter<S, ? super T> setter
    ) {
        return new Lens<>() {
            @Override
            public T get(S s) {
                return getter.get(s);
            }

            @Override
            public S set(S s, T val) {
                return setter.set(s, val);
            }
        };
    }

    static <S> Lens.OfBoolean<S> of(
            Getter.OfBoolean<? super S> getter,
            Setter.OfBoolean<S> setter
    ) {
        return new OfBoolean<>() {
            @Override
            public boolean getBoolean(S s) {
                return getter.getBoolean(s);
            }

            @Override
            public S setBoolean(S s, boolean value) {
                return setter.setBoolean(s, value);
            }
        };
    }

    static <S> Lens.OfInt<S> of(
            Getter.OfInt<? super S> getter,
            Setter.OfInt<S> setter
    ) {
        return new OfInt<>() {
            @Override
            public int getInt(S s) {
                return getter.getInt(s);
            }

            @Override
            public S setInt(S s, int value) {
                return setter.setInt(s, value);
            }
        };
    }

    static <S> Lens.OfLong<S> of(
            Getter.OfLong<? super S> getter,
            Setter.OfLong<S> setter
    ) {
        return new OfLong<>() {
            @Override
            public long getLong(S s) {
                return getter.getLong(s);
            }

            @Override
            public S setLong(S s, long value) {
                return setter.setLong(s, value);
            }
        };
    }

    static <S> Lens.OfFloat<S> of(
            Getter.OfFloat<? super S> getter,
            Setter.OfFloat<S> setter
    ) {
        return new OfFloat<>() {
            @Override
            public float getFloat(S s) {
                return getter.getFloat(s);
            }

            @Override
            public S setFloat(S s, float value) {
                return setter.setFloat(s, value);
            }
        };
    }

    static <S> Lens.OfDouble<S> of(
            Getter.OfDouble<? super S> getter,
            Setter.OfDouble<S> setter
    ) {
        return new OfDouble<>() {
            @Override
            public double getDouble(S s) {
                return getter.getDouble(s);
            }

            @Override
            public S setDouble(S s, double value) {
                return setter.setDouble(s, value);
            }
        };
    }

    T get(S s);

    S set(S s, T val);

    interface OfBoolean<S> {
        boolean getBoolean(S s);

        S setBoolean(S s, boolean value);
    }

    interface OfInt<S> {
        int getInt(S s);

        S setInt(S s, int value);
    }

    interface OfLong<S> {
        long getLong(S s);

        S setLong(S s, long value);
    }

    interface OfFloat<S> {
        float getFloat(S s);

        S setFloat(S s, float value);
    }

    interface OfDouble<S> {
        double getDouble(S s);

        S setDouble(S s, double value);
    }
}

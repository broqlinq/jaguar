package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.ToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class StateSignal<T> extends StateSignalBase implements State<T> {

    private final BiPredicate<? super T, ? super T> equality;
    private T value;

    public StateSignal(ConfinedScope scope, T initial, BiPredicate<? super T, ? super T> equality) {
        super(scope);
        this.value = initial;
        this.equality = Objects.requireNonNull(equality, "equality");
    }

    @Override
    public void set(T newValue) {
        preWrite();

        T oldValue = value;
        if (equality.test(oldValue, newValue)) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public void update(UnaryOperator<T> updater) {
        Objects.requireNonNull(updater, "updater");

        preWrite();

        T oldValue = value;
        T newValue = updater.apply(oldValue);

        if (equality.test(oldValue, newValue)) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public T value() {
        preRead();
        return value;
    }

    private T valueUnchecked() {
        scope.track(this);
        return value;
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> fn) {
        return scope.computed(() -> fn.apply(valueUnchecked()));
    }

    @Override
    public BooleanSignal deriveBoolean(Predicate<? super T> fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(ToIntFunction<? super T> fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(ToLongFunction<? super T> fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(ToFloatFunction<? super T> fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(ToDoubleFunction<? super T> fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }

    @Override
    public <R> State<R> project(Lens<T, R> lens) {
        return null;
    }

    @Override
    public BooleanState project(Lens.OfBoolean<T> lens) {
        return null;
    }

    @Override
    public IntState project(Lens.OfInt<T> lens) {
        return null;
    }

    @Override
    public LongState project(Lens.OfLong<T> lens) {
        return null;
    }

    @Override
    public FloatState project(Lens.OfFloat<T> lens) {
        return null;
    }

    @Override
    public DoubleState project(Lens.OfDouble<T> lens) {
        return null;
    }
}

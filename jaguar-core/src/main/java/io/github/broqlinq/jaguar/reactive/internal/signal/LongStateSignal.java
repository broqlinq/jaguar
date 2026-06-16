package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.LongToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public class LongStateSignal extends StateSignalBase implements LongState {

    private long value;

    public LongStateSignal(ConfinedScope scope, long initial) {
        super(scope);
        this.value = initial;
    }

    @Override
    public void set(long newValue) {
        assertOpen();

        long oldValue = this.value;
        if (oldValue == newValue) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public void update(LongUnaryOperator updater) {
        assertOpen();
        Objects.requireNonNull(updater, "updater");

        long oldValue = this.value;
        long newValue = updater.applyAsLong(value);
        if (oldValue == newValue) {
            return;
        }

        value = newValue;
        commitStateChange();
    }

    @Override
    public long value() {
        assertOpen();
        scope.track(this);
        return value;
    }

    private long valueUnchecked() {
        scope.track(this);
        return value;
    }

    @Override
    public <T> Signal<T> derive(LongFunction<? extends T> fn, BiPredicate<? super T, ? super T> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public BooleanSignal deriveBoolean(LongPredicate fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(LongToIntFunction fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(LongUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(LongToFloatFunction fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(LongToDoubleFunction fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

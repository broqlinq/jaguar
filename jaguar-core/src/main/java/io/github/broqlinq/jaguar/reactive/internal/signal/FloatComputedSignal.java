package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.*;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.BiPredicate;

public final class FloatComputedSignal extends ComputedSignalBase implements FloatSignal {

    private final FloatSupplier supplier;

    private float cachedValue;

    public FloatComputedSignal(ConfinedScope scope,  FloatSupplier supplier) {
        super(scope);
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public float value() {
        assertOpen();
        return valueUnchecked();
    }

    private float valueUnchecked() {
        trackRead();
        validateComputed();
        return  cachedValue;
    }

    @Override
    protected boolean recomputeValue() {
        float oldValue = this.cachedValue;
        float newValue = supplier.getAsFloat();

        if (isInitialized() && oldValue == newValue) {
            return false;
        }

        this.cachedValue = newValue;
        return true;
    }

    @Override
    public <R> Signal<R> derive(FloatFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public BooleanSignal deriveBoolean(FloatPredicate fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(FloatToIntFunction fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(FloatToLongFunction fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(FloatUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(FloatToDoubleFunction fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

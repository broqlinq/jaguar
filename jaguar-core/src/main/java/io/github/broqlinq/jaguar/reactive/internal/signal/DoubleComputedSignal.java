package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.DoubleToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class DoubleComputedSignal extends ComputedSignalBase implements DoubleSignal {

    private final DoubleSupplier supplier;

    private double cachedValue;

    public DoubleComputedSignal(ConfinedScope scope, DoubleSupplier supplier) {
        super(scope);
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public double value() {
        assertOpen();
        return valueUnchecked();
    }

    private double valueUnchecked() {
        trackRead();
        validateComputed();
        return cachedValue;
    }

    @Override
    protected boolean recomputeValue() {
        double oldValue = cachedValue;
        double newValue = supplier.getAsDouble();

        if (isInitialized() && oldValue == newValue) {
            return false;
        }

        cachedValue = newValue;
        return true;
    }

    @Override
    public <R> Signal<R> derive(DoubleFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public BooleanSignal deriveBoolean(DoublePredicate fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(DoubleToIntFunction fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(DoubleToLongFunction fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(DoubleToFloatFunction fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(DoubleUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

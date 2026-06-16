package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.*;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

public final class BooleanComputedSignal extends ComputedSignalBase implements BooleanSignal {

    private final BooleanSupplier supplier;

    private boolean cachedValue;

    public BooleanComputedSignal(ConfinedScope scope, BooleanSupplier supplier) {
        super(scope);
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public boolean value() {
        assertOpen();
        return valueUnchecked();
    }

    private boolean valueUnchecked() {
        trackRead();
        validateComputed();
        return cachedValue;
    }

    @Override
    protected boolean recomputeValue() {
        var oldValue = this.cachedValue;
        var newValue = supplier.getAsBoolean();

        if (isInitialized() && oldValue ==  newValue) {
            return false;
        }

        cachedValue = newValue;
        return true;
    }

    @Override
    public <R> Signal<R> derive(BooleanFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public BooleanSignal deriveBoolean(BooleanUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsBoolean(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(BooleanToIntFunction fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(BooleanToLongFunction fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(BooleanToFloatFunction fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(BooleanToDoubleFunction fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

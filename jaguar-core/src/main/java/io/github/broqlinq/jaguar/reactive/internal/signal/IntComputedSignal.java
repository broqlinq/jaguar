package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.IntToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class IntComputedSignal extends ComputedSignalBase implements IntSignal {

    private final IntSupplier supplier;

    private int cachedValue;

    public IntComputedSignal(ConfinedScope scope, IntSupplier supplier) {
        super(scope);
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public int value() {
        assertOpen();
        return valueUnchecked();
    }

    private int valueUnchecked() {
        trackRead();
        validateComputed();
        return cachedValue;
    }

    @Override
    protected boolean recomputeValue() {
        int oldValue = this.cachedValue;
        int newValue = supplier.getAsInt();

        if (isInitialized() && oldValue == newValue) {
            return false;
        }

        this.cachedValue = newValue;
        return true;
    }

    @Override
    public <R> Signal<R> derive(IntFunction<? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
    }

    @Override
    public BooleanSignal deriveBoolean(IntPredicate fn) {
        return scope.computed(() -> fn.test(valueUnchecked()));
    }

    @Override
    public IntSignal deriveInt(IntUnaryOperator fn) {
        return scope.computed(() -> fn.applyAsInt(valueUnchecked()));
    }

    @Override
    public LongSignal deriveLong(IntToLongFunction fn) {
        return scope.computed(() -> fn.applyAsLong(valueUnchecked()));
    }

    @Override
    public FloatSignal deriveFloat(IntToFloatFunction fn) {
        return scope.computed(() -> fn.applyAsFloat(valueUnchecked()));
    }

    @Override
    public DoubleSignal deriveDouble(IntToDoubleFunction fn) {
        return scope.computed(() -> fn.applyAsDouble(valueUnchecked()));
    }
}

package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.LongToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class LongComputedSignal extends ComputedSignalBase implements LongSignal {

    private final LongSupplier supplier;

    private long cachedValue;

    public LongComputedSignal(ConfinedScope scope, LongSupplier supplier) {
        super(scope);
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public long value() {
        assertOpen();
        return valueUnchecked();
    }

    private long valueUnchecked() {
        trackRead();
        validateComputed();
        return cachedValue;
    }

    @Override
    protected boolean recomputeValue() {
        long oldValue = this.cachedValue;
        long newValue = supplier.getAsLong();

        if (isInitialized() && oldValue == newValue) {
            return false;
        }

        this.cachedValue = newValue;
        return true;
    }

    @Override
    public <T> Signal<T> derive(LongFunction<? extends T> fn, BiPredicate<? super T, ? super T> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()));
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

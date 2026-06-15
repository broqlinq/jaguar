package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.function.ToFloatFunction;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Objects;
import java.util.function.*;

public final class ComputedSignal<T> extends ComputedSignalBase implements Signal<T> {

    private final Supplier<? extends T> supplier;
    private final BiPredicate<? super T, ? super T> equality;

    private T cachedValue;

    public ComputedSignal(ConfinedScope scope, Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> equality) {
        super(scope);
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.equality = Objects.requireNonNull(equality, "equality");
    }

    @Override
    public T value() {
        preRead();
        return cachedValue;
    }

    private T valueUnchecked() {
        trackRead();
        validateComputed();
        return cachedValue;
    }

    @Override
    protected boolean recomputeValue() {
        T oldValue = cachedValue;
        T newValue = supplier.get();

        if (isInitialized() && equality.test(oldValue, newValue)) {
            return false;
        }

        cachedValue = newValue;
        return true;
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> fn, BiPredicate<? super R, ? super R> equality) {
        return scope.computed(() -> fn.apply(valueUnchecked()), equality);
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
}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.IntSignal;
import io.github.broqlinq.jaguar.reactive.Signal;

import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

final class ScopedIntConstant extends ReactiveNode implements IntSignal {

    private final int value;

    ScopedIntConstant(ThreadConfinedScope scope, int value) {
        super(scope);
        this.value = value;
    }

    @Override
    public int intValue() {
        assertReadable();
        return value;
    }

    @Override
    public <R> Signal<R> derive(IntFunction<? extends R> mapper) {
        assertReadable();
        return scope.computed(() -> mapper.apply(value));
    }

    @Override
    public IntSignal deriveInt(IntUnaryOperator mapper) {
        assertReadable();
        return scope.intConstant(mapper.applyAsInt(value));
    }
}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Signal;

import java.util.Objects;
import java.util.function.Function;

final class ScopedConstant<T> extends ScopeOwned implements Signal<T> {

    private final T value;
    private boolean disposed = false;

    ScopedConstant(ScopeContext context, T value) {
        super(context);
        this.value = value;
    }

    @Override
    public T value() {
        assertReadable("Attempted read from disposed signal");
        return value;
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        assertReadable("Attempted read from disposed signal");
        return context.constant(mapper.apply(value()));
    }

    @Override
    public <R> R transform(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        assertReadable("Attempted transform from disposed signal");
        return mapper.apply(value());
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed || context().isClosed();
    }
}

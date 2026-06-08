package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.reactive.State;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

final class ScopedState<T> extends ProducerNode implements State<T> {

    private final Runnable notifyTask = computedLinks::markDependentsInvalid;

    private final BiPredicate<? super T, ? super T> eq;
    private T value;

    ScopedState(ThreadConfinedScope scope, T initialValue, BiPredicate<? super T, ? super T> eq) {
        this.value = initialValue;
        this.eq = Objects.requireNonNull(eq, "eq");
        super(scope);
    }

    @Override
    public void set(T value) {
        assertWritable();
        setUnchecked(value);
    }

    void setUnchecked(T newValue) {
        final T oldValue = this.value;
        if (eq.test(oldValue, newValue)) {
            return;
        }
        this.value = newValue;
        updateVersion();
        batchNotifyDependents();
    }

    private void batchNotifyDependents() {
        if (computedLinks().nonEmpty()) {
            scope.batch(notifyTask);
        }
    }

    @Override
    public T value() {
        assertReadable();
        return valueUnchecked();
    }

    T valueUnchecked() {
//        scope.trackingContext().track(this);
        final var current = scope.currentConsumer;
        if (current != null) {
            var link = current.producerLinks().getOrCreate(this);
            if (link.active) {
                link.markSeen();
            } else {
                link.link(this, current);
            }
        }
        return value;
    }

    @Override
    public <R> Signal<R> derive(Function<? super T, ? extends R> mapper) {
        assertReadable();
        Objects.requireNonNull(mapper, "mapper");
        return scope.computed(() -> mapper.apply(valueUnchecked()));
    }

    @Override
    protected void onDispose() {
        scope.removeState(this);
    }

    @Override
    public ConsumerList effectLinks() {
        return null;
    }
}

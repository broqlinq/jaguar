package io.github.broqlinq.jaguar.core.internal.signal;

import io.github.broqlinq.jaguar.core.signal.State;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

class StateImpl<T> implements State<T> {

    private static final VarHandle VALUE_HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            VALUE_HANDLE = lookup.findVarHandle(StateImpl.class, "value", Object.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final Set<Observer> observers = ConcurrentHashMap.newKeySet();

    private final BiPredicate<? super T, ? super T> equalityChecker;

    private T value;

    StateImpl(BiPredicate<? super T, ? super T> equalityChecker) {
        this.equalityChecker = Objects.requireNonNull(equalityChecker, "equality checker must not be null");
    }

    StateImpl() {
        this.equalityChecker = null;
    }

    @Override
    public void set(T newValue) {
        setValue(newValue, equalityChecker);
    }

    @Override
    public void set(T newValue, BiPredicate<? super T, ? super T> equalityChecker) {
        Objects.requireNonNull(equalityChecker, "equality checker must not be null");
        setValue(newValue, equalityChecker);
    }

    @Override
    public void set(Supplier<? extends T> supplier) {
        set(supplier.get());
    }

    @Override
    public void set(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> equalityChecker) {
        set(supplier.get(), equalityChecker);
    }

    private void setValue(T newValue, BiPredicate<? super T, ? super T> equalityChecker) {
        T oldValue;
        do {
            oldValue = getValue();
            var valuesEqual = equalityChecker == null ? oldValue == newValue : equalityChecker.test(oldValue, newValue);

            if (valuesEqual) {
                return;
            }
        } while (!VALUE_HANDLE.compareAndSet(this, oldValue, newValue));

        ObserverSupport.invalidateAll(observers);
    }

    @Override
    public void update(UnaryOperator<T> updater) {
        Objects.requireNonNull(updater, "updater must not be null");
        updateValue(updater, equalityChecker);
    }

    @Override
    public void update(UnaryOperator<T> updater, BiPredicate<? super T, ? super T> equalityChecker) {
        Objects.requireNonNull(updater, "updater must not be null");
        Objects.requireNonNull(equalityChecker, "equality checker must not be null");
        updateValue(updater, equalityChecker);
    }

    private void updateValue(UnaryOperator<T> updater, BiPredicate<? super T, ? super T> equalityChecker) {
        T oldValue, newValue;
        do {
            oldValue = getValue();
            newValue = updater.apply(oldValue);

            var valuesEqual = equalityChecker == null ? oldValue == newValue : equalityChecker.test(oldValue, newValue);

            if (valuesEqual) {
                return;
            }
        } while (!VALUE_HANDLE.compareAndSet(this, oldValue, newValue));

        ObserverSupport.invalidateAll(observers);
    }

    @SuppressWarnings("unchecked")
    private T getValue() {
        return (T) VALUE_HANDLE.getAcquire(this);
    }

    @Override
    public T value() {
        var current = EvaluationContext.peek();
        if (current != null && observers.add(current)) {
            current.subscribe(() -> observers.remove(current));
        }
        return getValue();
    }
}

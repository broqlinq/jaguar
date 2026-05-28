package io.github.broqlinq.jaguar.core.signal;

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface State<T> extends Signal<T> {

    void set(T newValue);

    void set(T newValue, BiPredicate<? super T, ? super T> equalityChecker);

    void set(Supplier<? extends T> supplier);

    void set(Supplier<? extends T> supplier, BiPredicate<? super T, ? super T> equalityChecker);

    void update(UnaryOperator<T> updater);

    void update(UnaryOperator<T> updater, BiPredicate<? super T, ? super T> equalityChecker);

}

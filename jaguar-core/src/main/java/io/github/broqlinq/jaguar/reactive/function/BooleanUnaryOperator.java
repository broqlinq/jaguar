package io.github.broqlinq.jaguar.reactive.function;

import java.util.Objects;

@FunctionalInterface
public interface BooleanUnaryOperator {

    boolean applyAsBoolean(boolean operand);

    default BooleanUnaryOperator compose(BooleanUnaryOperator before) {
        Objects.requireNonNull(before);
        return b ->  applyAsBoolean(before.applyAsBoolean(b));
    }

    default BooleanUnaryOperator andThen(BooleanUnaryOperator after) {
        Objects.requireNonNull(after);
        return b -> after.applyAsBoolean(applyAsBoolean(b));
    }

    static BooleanUnaryOperator identity() {
        return b -> b;
    }

    static BooleanUnaryOperator negate() {
        return b -> !b;
    }
}

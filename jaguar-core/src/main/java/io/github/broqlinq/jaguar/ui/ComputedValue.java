package io.github.broqlinq.jaguar.ui;

import java.util.function.Supplier;

public record ComputedValue<T>(Supplier<? extends T> computation) implements Value<T> {
}

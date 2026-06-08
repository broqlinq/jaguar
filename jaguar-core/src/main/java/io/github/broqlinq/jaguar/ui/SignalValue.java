package io.github.broqlinq.jaguar.ui;

import io.github.broqlinq.jaguar.reactive.Signal;

public record SignalValue<T>(Signal<T> signal) implements Value<T> {
}

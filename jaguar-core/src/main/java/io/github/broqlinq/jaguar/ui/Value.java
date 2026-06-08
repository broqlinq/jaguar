package io.github.broqlinq.jaguar.ui;

public sealed interface Value<T> permits StaticValue, SignalValue, ComputedValue {

}

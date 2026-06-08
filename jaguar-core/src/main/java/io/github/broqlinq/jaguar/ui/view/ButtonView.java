package io.github.broqlinq.jaguar.ui.view;

import io.github.broqlinq.jaguar.ui.Value;

public record ButtonView(Value<String> text, Runnable onClick) implements View {


}

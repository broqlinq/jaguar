package io.github.broqlinq.jaguar.ui.node;

import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.ui.*;
import io.github.broqlinq.jaguar.ui.component.ComponentContext;

public final class TextNode implements Node {

    private Signal<String> textSignal;
    private String text;

    public TextNode(Value<String> textValue, ComponentContext ctx) {
        switch (textValue) {
            case ComputedValue<String>(var supplier) -> {
                textSignal = ctx.computed(supplier);
                ctx.effect(() -> text = textSignal.get());
            }
            case SignalValue<String>(var signal) -> {
                textSignal = signal;
                ctx.effect(() -> text = textSignal.get());
            }
            case StaticValue<String>(var t) -> this.text = t;
        };
    }

    public Signal<String> getTextSignal() {
        return textSignal;
    }

    public String getText() {
        return text;
    }

    @Override
    public void dispose() {
        IO.println("Dispose Text node");
    }
}

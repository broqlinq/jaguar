package io.github.broqlinq.jaguar.ui.node;

import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.ui.*;
import io.github.broqlinq.jaguar.ui.component.ComponentContext;

public final class ButtonNode implements Node {

    private Signal<String> textSignal;
    private String text;
    private final Runnable onClick;

    public ButtonNode(Value<String> textValue, Runnable onClick, ComponentContext ctx) {
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
        this.onClick = onClick;
    }

    public void click() {
        onClick.run();
    }

    public Signal<String> getTextSignal() {
        return textSignal;
    }

    public String getText() {
        return text;
    }

    public Runnable getOnClick() {
        return onClick;
    }

    @Override
    public void dispose() {
        IO.println("Dispose Button node");
    }
}

package io.github.broqlinq.jaguar.ui.component;

import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.ui.ComputedValue;
import io.github.broqlinq.jaguar.ui.SignalValue;
import io.github.broqlinq.jaguar.ui.StaticValue;
import io.github.broqlinq.jaguar.ui.view.ButtonView;
import io.github.broqlinq.jaguar.ui.view.View;

import java.util.function.Supplier;

public class Button implements Component {

    @Override
    public View compose(ComponentContext context) {
        return null;
    }

    public static ButtonView of(String text, Runnable onClick) {
        return new ButtonView(new StaticValue<>(text), onClick);
    }

    public static ButtonView of(Signal<String> text, Runnable onClick) {
        return new ButtonView(new SignalValue<>(text), onClick);
    }

    public static ButtonView of(Supplier<String> text, Runnable onClick) {
        return new ButtonView(new ComputedValue<>(text), onClick);
    }
}

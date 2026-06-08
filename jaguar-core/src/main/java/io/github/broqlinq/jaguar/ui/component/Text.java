package io.github.broqlinq.jaguar.ui.component;

import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.ui.ComputedValue;
import io.github.broqlinq.jaguar.ui.SignalValue;
import io.github.broqlinq.jaguar.ui.StaticValue;
import io.github.broqlinq.jaguar.ui.view.TextView;
import io.github.broqlinq.jaguar.ui.view.View;

import java.util.function.Supplier;

public class Text implements Component {

    @Override
    public View compose(ComponentContext context) {
        // Ignore for now, this is primitive component
        return null;
    }

    public static TextView of(String text) {
        return new TextView(new StaticValue<>(text));
    }

    public static TextView of(Signal<String> textSignal) {
        return new TextView(new SignalValue<>(textSignal));
    }

    public static TextView of(Supplier<String> textSupplier) {
        return new TextView(new ComputedValue<>(textSupplier));
    }
}

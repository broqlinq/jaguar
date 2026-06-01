package io.github.broqlinq.jaguar.core.old.ui;

public interface StylableView<S extends StylableView<S>> extends View {

    S padding();

    S backgroundColor();

    S primaryColor();

    S font();

    S fontSize();

    S fontColor();
}

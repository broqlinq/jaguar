package io.github.broqlinq.jaguar.core.old.ui;

import io.github.broqlinq.jaguar.core.old.signal.State;

public class Button {

    private final State<String> label;
    // private final EventTarget.OfMouseEvent mouse = EventTarget.ofMouseEvent().accept(MouseEvent.ALL);
    // private final EventTarget.OfFocusEvent focus = EventTarget.ofFocusEvent();

    public Button(State<String> label) {
        this.label = label;
    }

    public View compose() {
        return Box.of(

        );
    }
}

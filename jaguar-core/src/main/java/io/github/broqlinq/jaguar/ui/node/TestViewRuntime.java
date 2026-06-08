package io.github.broqlinq.jaguar.ui.node;

import io.github.broqlinq.jaguar.ui.component.ComponentContext;
import io.github.broqlinq.jaguar.ui.view.ButtonView;
import io.github.broqlinq.jaguar.ui.view.TextView;
import io.github.broqlinq.jaguar.ui.view.VStackView;
import io.github.broqlinq.jaguar.ui.view.View;

public class TestViewRuntime implements ViewRuntime {

    @Override
    public Node materialize(View view, ComponentContext context) {
        return switch (view) {
            case ButtonView(var textValue, var onClick) -> new ButtonNode(textValue, onClick, context);
            case VStackView(var children) -> {
                var nodes = children.stream()
                        .map(v -> materialize(v, context))
                        .toList();
                yield new VStackNode(nodes);
            }
            case TextView(var textValue) -> new TextNode(textValue, context);
        };
    }

}

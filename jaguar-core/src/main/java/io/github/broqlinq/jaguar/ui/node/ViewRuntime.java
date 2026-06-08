package io.github.broqlinq.jaguar.ui.node;

import io.github.broqlinq.jaguar.ui.component.ComponentContext;
import io.github.broqlinq.jaguar.ui.view.View;

public interface ViewRuntime {

    Node materialize(View view, ComponentContext context);
}

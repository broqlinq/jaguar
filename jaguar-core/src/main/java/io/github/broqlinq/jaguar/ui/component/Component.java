package io.github.broqlinq.jaguar.ui.component;

import io.github.broqlinq.jaguar.ui.view.View;

public interface Component {

    View compose(ComponentContext context);
}

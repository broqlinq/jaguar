package io.github.broqlinq.jaguar.ui.component;

import io.github.broqlinq.jaguar.ui.view.VStackView;
import io.github.broqlinq.jaguar.ui.view.View;

import java.util.List;

public class VStack implements Component {

    @Override
    public View compose(ComponentContext context) {
        // Ignore for now, this is primitive component
        return null;
    }

    public static VStackView of(View... children) {
        List<View> childrenViews;
        if (children == null) {
            childrenViews = List.of();
        } else {
            childrenViews = List.of(children);
        }
        return new VStackView(childrenViews);
    }
}

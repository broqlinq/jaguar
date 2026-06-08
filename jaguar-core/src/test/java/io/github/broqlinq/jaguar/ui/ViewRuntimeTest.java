package io.github.broqlinq.jaguar.ui;

import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.State;
import io.github.broqlinq.jaguar.reactive.internal.ThreadConfinedScheduler;
import io.github.broqlinq.jaguar.reactive.internal.ThreadConfinedScope;
import io.github.broqlinq.jaguar.ui.component.Button;
import io.github.broqlinq.jaguar.ui.component.ComponentContext;
import io.github.broqlinq.jaguar.ui.component.Text;
import io.github.broqlinq.jaguar.ui.component.VStack;
import io.github.broqlinq.jaguar.ui.node.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

final class ViewRuntimeTest {

    private static Scope scope() {
        return new ThreadConfinedScope(
                "ui-test",
                new ThreadConfinedScheduler(Thread.currentThread())
        );
    }

    private static ComponentContext context(Scope scope) {
        return () -> scope;
    }

    @Test
    void textNodeUsesStaticValueOnce() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        Node node = runtime.materialize(Text.of("Hello"), context(scope));

        assertInstanceOf(TextNode.class, node);
        assertEquals("Hello", ((TextNode) node).getText());
    }

    @Test
    void textNodeTracksSignalValue() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        State<String> title = scope.state("Initial", Objects::equals);

        Node node = runtime.materialize(Text.of(title), context(scope));

        assertInstanceOf(TextNode.class, node);
        assertEquals("Initial", ((TextNode) node).getText());

        title.set("Updated");

        assertEquals("Updated", ((TextNode) node).getText());
    }

    @Test
    void textNodeTracksComputedValue() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        State<Integer> count = scope.state(0, Objects::equals);

        Node node = runtime.materialize(
                Text.of(() -> "Count: " + count.value()),
                context(scope)
        );

        assertInstanceOf(TextNode.class, node);
        assertEquals("Count: 0", ((TextNode) node).getText());

        count.set(1);

        assertEquals("Count: 1", ((TextNode) node).getText());
    }

    @Test
    void computedTextValueIsScopedToMaterializationContext() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        State<Integer> count = scope.state(10, Objects::equals);

        Node node = runtime.materialize(
                Text.of(() -> "Value: " + count.value()),
                context(scope)
        );

        assertInstanceOf(TextNode.class, node);
        assertEquals("Value: 10", ((TextNode) node).getText());

        scope.close();

        assertThrows(IllegalStateException.class, () -> count.set(11));
        assertEquals("Value: 10", ((TextNode) node).getText());
    }

    @Test
    void buttonNodeUsesStaticTextAndStoresClickHandler() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        int[] clicks = {0};

        Node node = runtime.materialize(
                Button.of("Click", () -> clicks[0]++),
                context(scope)
        );

        assertInstanceOf(ButtonNode.class, node);

        ButtonNode button = (ButtonNode) node;

        assertEquals("Click", button.getText());

        button.getOnClick().run();

        assertEquals(1, clicks[0]);
    }

    @Test
    void buttonNodeTracksSignalText() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        State<String> label = scope.state("Save", Objects::equals);

        Node node = runtime.materialize(
                Button.of(label, () -> {}),
                context(scope)
        );

        assertInstanceOf(ButtonNode.class, node);

        ButtonNode button = (ButtonNode) node;

        assertEquals("Save", button.getText());

        label.set("Saving...");

        assertEquals("Saving...", button.getText());
    }

    @Test
    void buttonNodeTracksComputedText() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        State<Boolean> saving = scope.state(false, Objects::equals);

        Node node = runtime.materialize(
                Button.of(() -> saving.value() ? "Saving..." : "Save", () -> {}),
                context(scope)
        );

        assertInstanceOf(ButtonNode.class, node);

        ButtonNode button = (ButtonNode) node;

        assertEquals("Save", button.getText());

        saving.set(true);

        assertEquals("Saving...", button.getText());
    }

    @Test
    void vStackMaterializesChildrenInOrder() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        Node node = runtime.materialize(
                VStack.of(
                        Text.of("A"),
                        Text.of("B")
                ),
                context(scope)
        );

        assertInstanceOf(VStackNode.class, node);

        VStackNode stack = (VStackNode) node;
        List<Node> children = stack.getChildren();

        assertEquals(2, children.size());

        assertInstanceOf(TextNode.class, children.get(0));
        assertInstanceOf(TextNode.class, children.get(1));

        assertEquals("A", ((TextNode) children.get(0)).getText());
        assertEquals("B", ((TextNode) children.get(1)).getText());
    }

    @Test
    void vStackWithNullChildrenCreatesEmptyStack() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        Node node = runtime.materialize(
                VStack.of((io.github.broqlinq.jaguar.ui.view.View[]) null),
                context(scope)
        );

        assertInstanceOf(VStackNode.class, node);

        VStackNode stack = (VStackNode) node;

        assertTrue(stack.getChildren().isEmpty());
    }

    @Test
    void multipleNodesCanBindToSameSignal() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        State<String> shared = scope.state("One", Objects::equals);

        TextNode first = (TextNode) runtime.materialize(Text.of(shared), context(scope));
        TextNode second = (TextNode) runtime.materialize(Text.of(shared), context(scope));

        assertEquals("One", first.getText());
        assertEquals("One", second.getText());

        shared.set("Two");

        assertEquals("Two", first.getText());
        assertEquals("Two", second.getText());
    }

    @Test
    void disposingNodeDoesNotCurrentlyDetachReactiveBinding() {
        var scope = scope();
        var runtime = new TestViewRuntime();

        State<String> title = scope.state("A", Objects::equals);

        TextNode node = (TextNode) runtime.materialize(Text.of(title), context(scope));

        assertEquals("A", node.getText());

        node.dispose();

        title.set("B");

        assertEquals(
                "B",
                node.getText(),
                "Current Node.dispose() only disposes the node object; bindings belong to the passed component scope."
        );
    }
}

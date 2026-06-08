package io.github.broqlinq.jaguar.ui.node;

import java.util.ArrayList;
import java.util.List;

public final class VStackNode implements Node {

    private List<Node> children;

    public VStackNode(List<Node> children) {
        this.children = new ArrayList<>(children);
    }

    @Override
    public void dispose() {

        IO.println("Dispose VStack node");

        for (var node : children) {
            node.dispose();
        }
        children.clear();
    }

    public List<Node> getChildren() {
        return List.copyOf(children);
    }
}

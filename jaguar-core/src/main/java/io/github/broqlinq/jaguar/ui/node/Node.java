package io.github.broqlinq.jaguar.ui.node;

public sealed interface Node permits ButtonNode, TextNode, VStackNode {

    void dispose();
}

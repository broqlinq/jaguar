package io.github.broqlinq.jaguar.core.old.ui;

public class Box implements StylableView<Box> {

    public static Box of() {
        return new Box();
    }

    @Override
    public Node materialize(Context context) {
        return null;
    }

    @Override
    public Box padding() {
        return this;
    }

    @Override
    public Box backgroundColor() {
        return this;
    }

    @Override
    public Box primaryColor() {
        return this;
    }

    @Override
    public Box font() {
        return this;
    }

    @Override
    public Box fontSize() {
        return this;
    }

    @Override
    public Box fontColor() {
        return this;
    }
}

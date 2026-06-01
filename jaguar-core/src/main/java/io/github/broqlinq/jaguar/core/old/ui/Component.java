package io.github.broqlinq.jaguar.core.old.ui;

public abstract class Component implements View {

    protected abstract void onMount();

    protected abstract void onUnmount();

    public abstract View compose();

}

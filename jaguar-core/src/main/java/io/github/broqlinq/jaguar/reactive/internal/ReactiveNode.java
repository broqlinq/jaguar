package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scope;

sealed abstract class ReactiveNode implements Scoped permits ConsumerNode, ProducerNode, ScopedIntConstant {

    protected final ThreadConfinedScope scope;
    private boolean disposed;

    protected ReactiveNode(ThreadConfinedScope scope) {
        this.scope = scope;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    public boolean isDisposed() {
        return disposed || scope.isClosed();
    }

    public final void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        onDispose();
    }

    protected void onDispose() {}

    protected final void assertReadable() {
        scope.assertOwnerThread();

        if (isDisposed()) {
            throw new IllegalStateException("Attempted read from disposed node");
        }
    }

    protected final void assertWritable() {
        scope.assertOwnerThread();

        if (isDisposed()) {
            throw new IllegalStateException("Attempted write to disposed node");
        }
    }
}

package io.github.broqlinq.jaguar.reactive.internal;

public abstract class ScopeResource extends ScopeSession.Resource {

    protected final ConfinedScope scope;

    public ScopeResource(ConfinedScope scope) {
        this.scope = scope;
    }

    protected final void assertNotDisposed() {
        if (isDisposed()) {
            throw new IllegalStateException("Resource is already disposed");
        }
    }

    protected final void assertOpen() {
        if (isDisposed()) {
            throw new IllegalStateException("Resource is already disposed");
        }
        if (scope.isClosed()) {
            throw new IllegalStateException("Resource scope is closed");
        }
    }

    @Override
    protected void onDispose() {

    }
}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class ScopeOwned implements Scoped {

    protected final ThreadConfinedScope scope;
    protected final List<Runnable> cleanup = new ArrayList<>(1);

    public ScopeOwned(ThreadConfinedScope scope) {
        this.scope = Objects.requireNonNull(scope, "context");
    }

    @Override
    public Scope scope() {
        return scope;
    }

    public boolean isDisposed() {
        return scope.isClosed();
    }

    protected void assertNotDisposed(String message) {
        if (isDisposed()) {
            throw new IllegalStateException(message);
        }
    }

    protected void assertReadable(String message) {
        scope.assertOwnerThread();
        assertNotDisposed(message);
    }

    protected void assertWritable(String message) {
        scope.assertOwnerThread();
        assertNotDisposed(message);
    }

    protected final void onDispose(Runnable cleanup) {
        this.cleanup.add(cleanup);
    }

    protected final void cleanup() {
        Throwable fail = null;
        for (var cleanup : cleanup) {
            try {
                cleanup.run();
            } catch (Throwable ex) {
                if (fail == null) {
                    fail = ex;
                } else {
                    fail.addSuppressed(ex);
                }
            }
        }
        if (fail != null) {
            throw new RuntimeException(fail);
        }
    }
}

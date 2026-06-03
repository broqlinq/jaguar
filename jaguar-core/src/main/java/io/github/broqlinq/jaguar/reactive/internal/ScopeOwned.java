package io.github.broqlinq.jaguar.reactive.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class ScopeOwned implements Scoped {

    protected final ScopeContext context;
    protected final List<Runnable> cleanup = new ArrayList<>(1);

    public ScopeOwned(ScopeContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public ScopeContext context() {
        return context;
    }

    public boolean isDisposed() {
        return context.isClosed();
    }

    protected void assertNotDisposed(String message) {
        if (isDisposed()) {
            throw new IllegalStateException(message);
        }
    }

    protected void assertReadable(String message) {
        context.assertOwnerThread();
        assertNotDisposed(message);
    }

    protected void assertWritable(String message) {
        context.assertOwnerThread();
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

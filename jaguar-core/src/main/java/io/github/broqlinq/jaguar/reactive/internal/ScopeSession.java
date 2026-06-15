package io.github.broqlinq.jaguar.reactive.internal;

import java.util.Objects;

public final class ScopeSession {

    private final ResourceList childScopes = new ResourceList();
    private final ResourceList effects = new ResourceList();
    private final ResourceList computedSignals = new ResourceList();
    private final ResourceList stateSignals = new ResourceList();

    private Status status;

    ScopeSession() {
        this.status = Status.OPEN;
    }

    void close() {
        if (status != Status.OPEN) {
            return;
        }

        status = Status.CLOSING;
        try {
            runCleanups();
        } finally {
            status = Status.CLOSED;
        }
    }

    void addScopeResource(Resource resource) {
        assertOpen();
        childScopes.add(resource);
    }

    void addEffectResource(Resource resource) {
        assertOpen();
        effects.add(resource);
    }

    void addComputedResource(Resource resource) {
        assertOpen();
        computedSignals.add(resource);
    }

    void addStateResource(Resource resource) {
        assertOpen();
        stateSignals.add(resource);
    }

    private void assertOpen() {
        if (status != Status.OPEN) {
            throwAlreadyClosed();
        }
    }

    private void throwAlreadyClosed() {
        throw new IllegalStateException("Attempted to access non-open session");
    }

    private void runCleanups() {
        var accumulator = new ExceptionAccumulator();
        accumulator.runTask(childScopes)
                .runTask(effects)
                .runTask(computedSignals)
                .runTask(stateSignals)
                .throwIfFailed();
    }

    enum Status {
        OPEN,
        CLOSING,
        CLOSED
    }

    private static final class ResourceList implements Runnable {

        Resource head;
        private boolean cleaned = false;

        private static void throwAlreadyCleaned() {
            throw new IllegalStateException("Resource list already closed");
        }

        void add(Resource resource) {
            Objects.requireNonNull(resource, "resource");
            assertOpen();

            if (resource.owner != null && resource.owner != this) {
                throwNotOwner();
            }

            if (resource.prev != null || resource.next != null || resource == head) {
                throwAlreadyLinked();
            }

            resource.owner = this;
            resource.next = head;

            if (head != null) {
                head.prev = resource;
            }

            head = resource;
        }

        private void throwAlreadyLinked() {
            throw new IllegalStateException("Resource already in list");
        }

        void remove(Resource cleanup) {
            Objects.requireNonNull(cleanup, "cleanup");

            if (cleanup.owner == null) {
                return;
            }

            if (cleanup.owner != this) {
                throwNotOwner();
            }

            detach(cleanup);
        }

        private void detach(Resource resource) {
            var prev = resource.prev;
            var next = resource.next;

            if (prev != null) {
                prev.next = next;
            } else {
                head = next;
            }

            if (next != null) {
                next.prev = prev;
            }

            resource.unlink();
        }

        private void throwNotOwner() {
            throw new IllegalArgumentException("Resource not owned by this list");
        }

        private void assertOpen() {
            if (cleaned) {
                throwAlreadyCleaned();
            }
        }

        private void runCleanup() {
            var accumulator = new ExceptionAccumulator();
            var current = head;
            head = null;
            while (current != null) {
                var next = current.next;
                accumulator.runTask(current::dispose);
                current = next;
            }
            accumulator.throwIfFailed();
        }

        @Override
        public void run() {
            if (cleaned) {
                return;
            }
            cleaned = true;
            runCleanup();
        }

    }

    static abstract class Resource implements Disposable {
        private ResourceList owner;
        private Resource prev;
        private Resource next;
        private boolean disposed = false;

        final void removeSelf() {
            var owner = this.owner;
            if (owner != null) {
                owner.remove(this);
            }
        }

        final void unlink() {
            owner = null;
            prev = null;
            next = null;
        }

        @Override
        public final void dispose() {
            if (disposed) {
                return;
            }

            disposed = true;
            removeSelf();
            onDispose();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        protected abstract void onDispose();
    }
}

package io.github.broqlinq.jaguar.reactive.internal;

import static java.util.Objects.requireNonNull;

final class ConfinedSession {

    private final ResourceList childScopes = new ResourceList();
    private final ResourceList effects = new ResourceList();
    private final ResourceList computedSignals = new ResourceList();
    private final ResourceList stateSignals = new ResourceList();

    private Status status;

    ConfinedSession() {
        this.status = Status.OPEN;
    }

    private static ResourceList.ResourceCleanup ofRunnable(Runnable cleanup) {
        requireNonNull(cleanup, "cleanup");
        return ResourceList.ResourceCleanup.ofRunnable(cleanup);
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

    void addScopeCleanup(Runnable cleanup) {
        assertOpen();
        childScopes.add(ofRunnable(cleanup));
    }

    void addEffectCleanup(Runnable cleanup) {
        assertOpen();
        effects.add(ofRunnable(cleanup));
    }

    void addComputedCleanup(Runnable cleanup) {
        assertOpen();
        computedSignals.add(ofRunnable(cleanup));
    }

    void addStateCleanup(Runnable cleanup) {
        assertOpen();
        stateSignals.add(ofRunnable(cleanup));
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

    static final class ResourceList implements Runnable {

        ResourceCleanup head;
        private boolean cleaned = false;

        private static void throwAlreadyCleaned() {
            throw new IllegalStateException("Resource list already closed");
        }

        void add(ResourceCleanup cleanup) {
            if (cleaned) {
                throwAlreadyCleaned();
            }
            cleanup.next = head;
            head = cleanup;
        }

        void runCleanup() {
            var accumulator = new ExceptionAccumulator();
            var current = head;
            head = null;
            while (current != null) {
                var next = current.next;
                current.next = null;

                accumulator.runTask(current);

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

        static abstract class ResourceCleanup implements Runnable {

            ResourceCleanup next;
            private boolean cleaned = false;

            static ResourceCleanup ofRunnable(Runnable cleanup) {
                return new ResourceCleanup() {
                    @Override
                    void cleanup() {
                        cleanup.run();
                    }
                };
            }

            abstract void cleanup();

            @Override
            public void run() {
                if (cleaned) {
                    return;
                }
                cleaned = true;
                cleanup();
            }
        }
    }
}

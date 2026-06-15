package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ReactiveNode;

abstract class ComputedSignalBase extends ReactiveNode {

    private static final byte CLEAN = 0;
    private static final byte STALE = 1;
    private static final byte INVALID = 2;
    private static final byte COMPUTING = 3;

    private byte status = INVALID;
    private boolean initialized;

    ComputedSignalBase(ConfinedScope scope) {
        super(scope);
    }

    @Override
    protected final byte dependentKind() {
        return DEPENDENT_SIGNAL;
    }

    final void preRead() {
        scope.assertOpen();
        trackRead();
        validateComputed();
    }

    protected final boolean isInitialized() {
        return initialized;
    }

    @Override
    protected final void validate() {
        validateComputed();
    }

    protected final void validateComputed() {
        if (status == CLEAN) {
            return;
        }

        if (status == COMPUTING) {
            throw new IllegalStateException("Circular dependency detected");
        }

        if (status == STALE) {
            if (!validateDependenciesAndCheckChanged()) {
                status = CLEAN;
                return;
            }

            status = INVALID;
        }

        recompute();
    }

    private void recompute() {
        status = COMPUTING;

        epoch++;

        var previous = scope.push(this);
        boolean completed = false;
        boolean changed = false;

        try {
            changed = recomputeValue();
            completed = true;
        } finally {
            scope.pop(previous);

            if (completed) {
                removeStaleDependencies();

                boolean firstRun = !initialized;
                initialized = true;
                status = CLEAN;

                if (!firstRun && changed) {
                    version++;
                    notifyDependentsInvalidated();
                }
            } else {
                status = INVALID;
            }
        }
    }

    @Override
    protected final void onDependencyInvalidated() {
        if (isDisposed() || status == INVALID) {
            return;
        }

        status = INVALID;
        notifyDependentsStale();
    }

    @Override
    protected final void onDependencyStale() {
        if (isDisposed() || status != CLEAN) {
            return;
        }

        status = STALE;
        notifyDependentsStale();
    }

    @Override
    protected void onDispose() {
        unlinkAllDependencies();
        unlinkAllDependents();
    }

    protected abstract boolean recomputeValue();
}
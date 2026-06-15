package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ReactiveNode;

public abstract sealed class EffectBase extends ReactiveNode permits ReactiveEffect {

    private final QueueableEffect queueableEffect = new QueueableEffect();

    private boolean invalidated = true;
    private boolean initialized;
    private boolean running;

    protected EffectBase(ConfinedScope scope) {
        super(scope);
    }

    @Override
    protected final byte dependentKind() {
        return DEPENDENT_EFFECT;
    }

    @Override
    public final void onDependencyInvalidated() {
        if (isDisposed()) {
            return;
        }

        invalidated = true;

        if (!running) {
            schedule();
        }
    }

    @Override
    protected final void onDependencyStale() {
        onDependencyInvalidated();
    }

    public final void schedule() {
        if (isDisposed() || queueableEffect.queued) {
            return;
        }

        queueableEffect.queued = true;
        scope.scheduleUnchecked(queueableEffect);
    }

    private void runIfNeeded() {
        if (isDisposed() || !invalidated) {
            return;
        }

        boolean completed = false;

        running = true;
        try {
            // Consume the invalidation that caused this scheduled run.
            invalidated = false;

            if (initialized && !validateDependenciesAndCheckChanged()) {
                completed = true;
                return;
            }

            // Computed validation may have notified this effect again.
            // This run is about to observe the latest values, so consume that too.
            invalidated = false;

            epoch++;

            final var previous = scope.push(this);
            try {
                runEffect();
                completed = true;
            } finally {
                scope.pop(previous);

                if (completed) {
                    removeStaleDependencies();
                    initialized = true;
                }
            }
        } finally {
            running = false;

            // If something invalidated this effect while user code was running,
            // schedule another pass. Do not auto-reschedule after failure.
            if (completed && invalidated && !isDisposed()) {
                schedule();
            }
        }
    }

    @Override
    protected void onDispose() {
        invalidated = false;
        unlinkAllDependencies();
    }

    protected abstract void runEffect();

    private final class QueueableEffect implements Runnable {

        private boolean queued;

        @Override
        public void run() {
            queued = false;
            runIfNeeded();
        }
    }
}

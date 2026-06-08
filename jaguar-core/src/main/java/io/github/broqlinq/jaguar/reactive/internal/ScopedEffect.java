package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Effect;
import static io.github.broqlinq.jaguar.reactive.internal.Consumer.State.*;

import java.util.Objects;

final class ScopedEffect extends ConsumerNode implements Effect {

    private final QueueableEffect effect;

    ScopedEffect(ThreadConfinedScope context, Runnable effect) {
        super(context, Type.EFFECT);
        Objects.requireNonNull(effect, "effect");
        this.effect = new QueueableEffect(effect);
        this.state = INVALID;
    }

    @Override
    public void markInvalid() {
        if (state == INVALID) {
            return;
        }
        state = INVALID;
        scheduleEffect();
    }

    @Override
    public void markStale() {
        if (state != CLEAN) {
            return;
        }
        state = STALE;
        scheduleEffect();
    }

    @Override
    protected void onDispose() {
        state = CLEAN;
        producerLinks.unlinkAll();
        scope.removeEffect(this);
    }

    void scheduleEffect() {
        if (isDisposed() || effect.queued) {
            return;
        }

        effect.queued = true;
        scope.schedule(effect);
    }

    private final class QueueableEffect implements Runnable {
        private final Runnable runnable;
        private boolean queued = false;

        QueueableEffect(Runnable effect) {
            this.runnable = effect;
        }

        @Override
        public void run() {
            queued = false;

            if (state == CLEAN || isDisposed()) {
                return;
            }

            if (state == STALE && !producerLinks.detectChanged()) {
                state = CLEAN;
                return;
            }

            updateEpoch();
            boolean completed = false;
//            scope.trackingContext().push(ScopedEffect.this);
//            try {
//                runnable.run();
//                completed = true;
//            } finally {
//                scope.trackingContext().pop(ScopedEffect.this);
//                if (completed) {
//                    producerLinks.removeStale();
//                    state = CLEAN;
//                }
//            }
            if (tracking) {
                throw new IllegalStateException("Circular dependency detected");
            }
            final var current = scope.currentConsumer;
            tracking = true;
            scope.currentConsumer = ScopedEffect.this;
            try {
                runnable.run();
                completed = true;
            } finally {
                scope.currentConsumer = current;
                tracking = false;
                if (completed) {
                    producerLinks.removeStale();
                    state = CLEAN;
                }
            }
        }
    }
}

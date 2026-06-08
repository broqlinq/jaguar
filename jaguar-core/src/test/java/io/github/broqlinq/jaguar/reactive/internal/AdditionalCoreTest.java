package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.reactive.State;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdditionalCoreTest {

    @Test
    void nestedComputedTrackingConnectsToImmediateConsumer() {
        Scope scope = new ThreadConfinedScope(
                "test",
                new ThreadConfinedScheduler(Thread.currentThread())
        );

        State<Integer> source = scope.state(0, Objects::equals);

        AtomicInteger c1Runs = new AtomicInteger();
        AtomicInteger c2Runs = new AtomicInteger();
        AtomicInteger effectRuns = new AtomicInteger();

        Signal<Integer> c1 = scope.computed(() -> {
            c1Runs.incrementAndGet();
            return source.value() + 1;
        }, Objects::equals);

        Signal<Integer> c2 = scope.computed(() -> {
            c2Runs.incrementAndGet();
            return c1.value() + 1;
        }, Objects::equals);

        scope.effect(() -> {
            effectRuns.incrementAndGet();
            c2.value();
        });

        int c1Initial = c1Runs.get();
        int c2Initial = c2Runs.get();
        int effectInitial = effectRuns.get();

        source.set(1);

        assertEquals(c1Initial + 1, c1Runs.get());
        assertEquals(c2Initial + 1, c2Runs.get());
        assertEquals(effectInitial + 1, effectRuns.get());

        source.set(2);

        assertEquals(c1Initial + 2, c1Runs.get());
        assertEquals(c2Initial + 2, c2Runs.get());
        assertEquals(effectInitial + 2, effectRuns.get());

        scope.close();
    }

    @Test
    void unchangedComputedDoesNotRerunDownstreamEffect() {
        Scope scope = new ThreadConfinedScope(
                "test",
                new ThreadConfinedScheduler(Thread.currentThread())
        );

        State<Integer> source = scope.state(0, Objects::equals);

        AtomicInteger parityRuns = new AtomicInteger();
        AtomicInteger derivedRuns = new AtomicInteger();
        AtomicInteger effectRuns = new AtomicInteger();

        Signal<Integer> parity = scope.computed(() -> {
            parityRuns.incrementAndGet();
            return source.value() & 1;
        }, Objects::equals);

        Signal<Integer> derived = scope.computed(() -> {
            derivedRuns.incrementAndGet();
            return parity.value() + 1;
        }, Objects::equals);

        scope.effect(() -> {
            effectRuns.incrementAndGet();
            derived.value();
        });

        int effectInitial = effectRuns.get();

        source.set(2);
        source.set(4);

        assertEquals(effectInitial, effectRuns.get());

        scope.close();
    }

    @Test
    void unchangedComputedAvoidsDownstreamRecomputeButMayRerunEffect() {
        Scope scope = new ThreadConfinedScope(
                "test",
                new ThreadConfinedScheduler(Thread.currentThread())
        );

        State<Integer> source = scope.state(0, Objects::equals);

        AtomicInteger parityRuns = new AtomicInteger();
        AtomicInteger derivedRuns = new AtomicInteger();
        AtomicInteger effectRuns = new AtomicInteger();

        Signal<Integer> parity = scope.computed(() -> {
            parityRuns.incrementAndGet();
            return source.value() & 1;
        }, Objects::equals);

        Signal<Integer> derived = scope.computed(() -> {
            derivedRuns.incrementAndGet();
            return parity.value() + 1;
        }, Objects::equals);

        scope.effect(() -> {
            effectRuns.incrementAndGet();
            derived.value();
        });

        int parityInitial = parityRuns.get();
        int derivedInitial = derivedRuns.get();
        int effectInitial = effectRuns.get();

        source.set(2);

        assertEquals(parityInitial + 1, parityRuns.get());

        /*
         * This is the key expectation:
         * if parity did not change, derived should not need to recompute.
         *
         * If this fails, equality-stop is not working deeply enough.
         */
        assertEquals(derivedInitial, derivedRuns.get());

        /*
         * Under eager stale propagation, the effect may rerun.
         */
        assertEquals(effectInitial + 1, effectRuns.get());

        scope.close();
    }
}

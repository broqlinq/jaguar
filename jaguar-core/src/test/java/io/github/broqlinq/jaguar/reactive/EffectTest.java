package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.signal.Signal;
import io.github.broqlinq.jaguar.reactive.signal.State;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class EffectTest {

    @Test
    void effectRunsInitially() {
        try (var scope = ReactiveTestSupport.newScope()) {
            var runs = new AtomicInteger();

            scope.effect(runs::incrementAndGet);

            assertEquals(1, runs.get());
        }
    }

    @Test
    void effectRerunsWhenStateDependencyChanges() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();
            var observed = new StringBuilder();

            scope.effect(() -> {
                observed.setLength(0);
                observed.append(state.value());
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());
            assertEquals("a", observed.toString());

            state.set("b");

            assertEquals(2, runs.get());
            assertEquals("b", observed.toString());
        }
    }

    @Test
    void effectDoesNotRerunWhenStateSetToEqualValue() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();

            scope.effect(() -> {
                state.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            state.set("a");

            assertEquals(1, runs.get());
        }
    }

    @Test
    void effectCanDependOnMultipleStates() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> first = scope.state("a", Objects::equals);
            State<String> second = scope.state("b", Objects::equals);

            var runs = new AtomicInteger();
            var observed = new StringBuilder();

            scope.effect(() -> {
                observed.setLength(0);
                observed.append(first.value()).append(second.value());
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());
            assertEquals("ab", observed.toString());

            first.set("x");

            assertEquals(2, runs.get());
            assertEquals("xb", observed.toString());

            second.set("y");

            assertEquals(3, runs.get());
            assertEquals("xy", observed.toString());
        }
    }

    @Test
    void effectRemovesStaleDependencies() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<Boolean> useA = scope.state(true, Objects::equals);
            State<String> a = scope.state("a1", Objects::equals);
            State<String> b = scope.state("b1", Objects::equals);

            var runs = new AtomicInteger();
            var observed = new StringBuilder();

            scope.effect(() -> {
                observed.setLength(0);
                observed.append(useA.value() ? a.value() : b.value());
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());
            assertEquals("a1", observed.toString());

            b.set("b2");

            assertEquals(1, runs.get(), "b is not currently a dependency");

            a.set("a2");

            assertEquals(2, runs.get());
            assertEquals("a2", observed.toString());

            useA.set(false);

            assertEquals(3, runs.get());
            assertEquals("b2", observed.toString());

            a.set("a3");

            assertEquals(3, runs.get(), "a should have been removed as stale dependency");

            b.set("b3");

            assertEquals(4, runs.get());
            assertEquals("b3", observed.toString());
        }
    }

    @Test
    void nestedComputedTrackingUsesImmediateConsumer() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<Integer> source = scope.state(1, Objects::equals);

            var c1Runs = new AtomicInteger();
            var c2Runs = new AtomicInteger();
            var effectRuns = new AtomicInteger();

            Signal<Integer> c1 = scope.computed(() -> {
                c1Runs.incrementAndGet();
                return source.value() + 1;
            }, Objects::equals);

            Signal<Integer> c2 = scope.computed(() -> {
                c2Runs.incrementAndGet();
                return c1.value() + 1;
            }, Objects::equals);

            scope.effect(() -> {
                c2.value();
                effectRuns.incrementAndGet();
            });

            assertEquals(1, c1Runs.get());
            assertEquals(1, c2Runs.get());
            assertEquals(1, effectRuns.get());

            source.set(2);

            assertEquals(2, c1Runs.get());
            assertEquals(2, c2Runs.get());
            assertEquals(2, effectRuns.get());

            source.set(3);

            assertEquals(3, c1Runs.get());
            assertEquals(3, c2Runs.get());
            assertEquals(3, effectRuns.get());
        }
    }

    @Test
    void disposedEffectDoesNotRerun() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();

            var effect = scope.effect(() -> {
                state.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            effect.dispose();

            state.set("b");

            assertEquals(1, runs.get());
        }
    }

    @Test
    void effectRejectsNullRunnable() {
        try (var scope = ReactiveTestSupport.newScope()) {
            assertThrows(NullPointerException.class, () -> scope.effect(null));
        }
    }

    @Test
    void effectFailureKeepsPreviousDependencies() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();

            scope.effect(() -> {
                state.value();
                int run = runs.incrementAndGet();

                if (run == 2) {
                    throw new IllegalStateException("boom");
                }
            });

            assertEquals(1, runs.get());

            assertThrows(IllegalStateException.class, () -> state.set("b"));

            assertEquals(2, runs.get());

            state.set("c");

            assertEquals(3, runs.get());
        }
    }
}

package io.github.broqlinq.jaguar.reactive;


import io.github.broqlinq.jaguar.reactive.signal.Signal;
import io.github.broqlinq.jaguar.reactive.signal.State;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class ComputedSignalTest {

    @Test
    void computedIsLazy() {
        try (var scope = ReactiveTestSupport.newScope()) {
            var calls = new AtomicInteger();

            Signal<String> computed = scope.computed(() -> {
                calls.incrementAndGet();
                return "value";
            }, Objects::equals);

            assertEquals(0, calls.get());

            assertEquals("value", computed.value());

            assertEquals(1, calls.get());
        }
    }

    @Test
    void computedCachesValueUntilDependencyChanges() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var calls = new AtomicInteger();

            Signal<String> computed = scope.computed(() -> {
                calls.incrementAndGet();
                return state.value().toUpperCase();
            }, Objects::equals);

            assertEquals("A", computed.value());
            assertEquals("A", computed.value());
            assertEquals("A", computed.value());

            assertEquals(1, calls.get());

            state.set("b");

            assertEquals("B", computed.value());
            assertEquals("B", computed.value());

            assertEquals(2, calls.get());
        }
    }

    @Test
    void computedDoesNotRecomputeWhenUnrelatedStateChanges() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> used = scope.state("a", Objects::equals);
            State<String> unused = scope.state("x", Objects::equals);

            var calls = new AtomicInteger();

            Signal<String> computed = scope.computed(() -> {
                calls.incrementAndGet();
                return used.value().toUpperCase();
            }, Objects::equals);

            assertEquals("A", computed.value());
            assertEquals(1, calls.get());

            unused.set("y");

            assertEquals("A", computed.value());
            assertEquals(1, calls.get());
        }
    }

    @Test
    void computedEqualitySuppressesDownstreamEffectRun() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<Integer> count = scope.state(1, Objects::equals);

            Signal<Integer> parity = scope.computed(
                    () -> count.value() % 2,
                    Objects::equals
            );

            var runs = new AtomicInteger();

            scope.effect(() -> {
                parity.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            count.set(3);

            assertEquals(1, runs.get(), "Effect should skip because computed dependency version did not change");

            count.set(4);

            assertEquals(2, runs.get());
        }
    }

    @Test
    void computedChainUpdatesCorrectly() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<Integer> source = scope.state(1, Objects::equals);

            Signal<Integer> plusOne = scope.computed(
                    () -> source.value() + 1,
                    Objects::equals
            );

            Signal<Integer> timesTwo = scope.computed(
                    () -> plusOne.value() * 2,
                    Objects::equals
            );

            assertEquals(4, timesTwo.value());

            source.set(2);

            assertEquals(6, timesTwo.value());
        }
    }

    @Test
    void computedRemovesStaleDependencies() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<Boolean> useA = scope.state(true, Objects::equals);
            State<String> a = scope.state("a1", Objects::equals);
            State<String> b = scope.state("b1", Objects::equals);

            Signal<String> selected = scope.computed(
                    () -> useA.value() ? a.value() : b.value(),
                    Objects::equals
            );

            var runs = new AtomicInteger();

            scope.effect(() -> {
                selected.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());
            assertEquals("a1", selected.value());

            b.set("b2");

            assertEquals(1, runs.get(), "b is not currently a dependency");

            a.set("a2");

            assertEquals(2, runs.get());
            assertEquals("a2", selected.value());

            useA.set(false);

            assertEquals(3, runs.get());
            assertEquals("b2", selected.value());

            a.set("a3");

            assertEquals(3, runs.get(), "a should have been removed as stale dependency");

            b.set("b3");

            assertEquals(4, runs.get());
            assertEquals("b3", selected.value());
        }
    }

    @Test
    void computedDetectsCircularDependency() {
        try (var scope = ReactiveTestSupport.newScope()) {
            @SuppressWarnings("unchecked")
            Signal<Integer>[] holder = new Signal[1];

            holder[0] = scope.computed(
                    () -> holder[0].value() + 1,
                    Objects::equals
            );

            assertThrows(IllegalStateException.class, holder[0]::value);
        }
    }

    @Test
    void computedDeriveCreatesDependentComputed() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> source = scope.state("abc", Objects::equals);

            Signal<Integer> length = source.derive(String::length);
            Signal<String> label = length.derive(value -> "len=" + value);

            assertEquals("len=3", label.value());

            source.set("abcdef");

            assertEquals("len=6", label.value());
        }
    }

    @Test
    void computedRejectsNullDeriveFunctions() {
        try (var scope = ReactiveTestSupport.newScope()) {
            Signal<String> computed = scope.computed(() -> "abc", Objects::equals);

            assertThrows(NullPointerException.class, () -> computed.derive(null));
            assertThrows(NullPointerException.class, () -> computed.deriveBoolean(null));
            assertThrows(NullPointerException.class, () -> computed.deriveInt(null));
            assertThrows(NullPointerException.class, () -> computed.deriveLong(null));
            assertThrows(NullPointerException.class, () -> computed.deriveFloat(null));
            assertThrows(NullPointerException.class, () -> computed.deriveDouble(null));
        }
    }
}

package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.signal.Signal;
import io.github.broqlinq.jaguar.reactive.signal.State;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

final class ComputedSignalPropertyTest {

    @Property
    void computedMatchesPureMapping(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> source = scope.state(initial, Objects::equals);

            Signal<Integer> computed = scope.computed(
                    () -> source.value() * 2 + 1,
                    Objects::equals
            );

            assertEquals(initial * 2 + 1, computed.value());

            for (int value : values) {
                source.set(value);

                assertEquals(value * 2 + 1, computed.value());
            }
        }
    }

    @Property
    void computedCachesBetweenDependencyChanges(
            @ForAll int initial,
            @ForAll @IntRange(min = 1, max = 100) int reads
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> source = scope.state(initial, Objects::equals);

            int[] calls = {0};

            Signal<Integer> computed = scope.computed(() -> {
                calls[0]++;
                return source.value() + 1;
            }, Objects::equals);

            assertEquals(0, calls[0]);

            for (int i = 0; i < reads; i++) {
                assertEquals(initial + 1, computed.value());
            }

            assertEquals(1, calls[0]);
        }
    }

    @Property
    void computedRecomputesOnlyAfterDistinctSourceChanges(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> source = scope.state(initial, Objects::equals);

            int[] calls = {0};

            Signal<Integer> computed = scope.computed(() -> {
                calls[0]++;
                return source.value() + 1;
            }, Objects::equals);

            assertEquals(initial + 1, computed.value());

            int expectedCalls = 1;
            int current = initial;

            for (int value : values) {
                boolean sourceChanged = current != value;

                source.set(value);

                assertEquals(value + 1, computed.value());

                if (sourceChanged) {
                    expectedCalls++;
                    current = value;
                }

                assertEquals(expectedCalls, calls[0]);
            }
        }
    }

    @Property
    void computedEqualitySuppressesEffectRuns(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> source = scope.state(initial, Objects::equals);

            Signal<Integer> parity = scope.computed(
                    () -> Math.floorMod(source.value(), 2),
                    Objects::equals
            );

            int[] runs = {0};
            int[] observed = {-1};

            scope.effect(() -> {
                observed[0] = parity.value();
                runs[0]++;
            });

            int currentSource = initial;
            int currentParity = Math.floorMod(initial, 2);
            int expectedRuns = 1;

            assertEquals(currentParity, observed[0]);
            assertEquals(expectedRuns, runs[0]);

            for (int value : values) {
                boolean sourceChanged = currentSource != value;
                int nextParity = Math.floorMod(value, 2);
                boolean parityChanged = sourceChanged && currentParity != nextParity;

                source.set(value);

                if (parityChanged) {
                    expectedRuns++;
                    currentParity = nextParity;
                }

                if (sourceChanged) {
                    currentSource = value;
                }

                assertEquals(currentParity, observed[0]);
                assertEquals(expectedRuns, runs[0]);
            }
        }
    }

    @Property
    void computedChainMatchesModel(
            @ForAll int initial,
            @ForAll @IntRange(min = 1, max = 20) int depth,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> source = scope.state(initial, Objects::equals);

            Signal<Integer> current = source;

            for (int i = 0; i < depth; i++) {
                Signal<Integer> previous = current;
                current = scope.computed(() -> previous.value() + 1, Objects::equals);
            }

            Signal<Integer> terminal = current;

            assertEquals(initial + depth, terminal.value());

            for (int value : values) {
                source.set(value);

                assertEquals(value + depth, terminal.value());
            }
        }
    }

    @Property
    void derivedComputedMatchesDirectComputed(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> source = scope.state(initial, Objects::equals);

            Signal<Integer> derived = source
                    .derive(value -> value + 10)
                    .derive(value -> value * 3);

            for (int value : values) {
                source.set(value);

                assertEquals((value + 10) * 3, derived.value());
            }

            assertEquals((source.value() + 10) * 3, derived.value());
        }
    }
}

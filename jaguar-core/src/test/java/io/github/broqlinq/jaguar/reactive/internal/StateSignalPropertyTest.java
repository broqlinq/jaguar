package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.State;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.StringLength;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

final class StateSignalPropertyTest {

    @Property
    void stateReflectsLastDistinctSet(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> state = scope.state(initial, Objects::equals);

            int expected = initial;
            assertEquals(expected, state.value());

            for (int value : values) {
                state.set(value);
                expected = value;

                assertEquals(expected, state.value());
            }
        }
    }

    @Property
    void stateUpdateMatchesPureModel(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<@IntRange(min = -20, max = 20) Integer> deltas
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> state = scope.state(initial, Objects::equals);

            int expected = initial;

            for (int delta : deltas) {
                state.update(value -> value + delta);
                expected += delta;

                assertEquals(expected, state.value());
            }
        }
    }

    @Property
    void equalSetDoesNotTriggerEffect(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> state = scope.state(initial, Objects::equals);

            int[] runs = {0};
            int[] observed = {Integer.MIN_VALUE};

            scope.effect(() -> {
                observed[0] = state.value();
                runs[0]++;
            });

            int expectedRuns = 1;
            int current = initial;

            assertEquals(expectedRuns, runs[0]);
            assertEquals(current, observed[0]);

            for (int value : values) {
                boolean changed = !Objects.equals(current, value);

                state.set(value);

                if (changed) {
                    expectedRuns++;
                    current = value;
                }

                assertEquals(expectedRuns, runs[0]);
                assertEquals(current, observed[0]);
            }
        }
    }

    @Property
    void customEqualityControlsStateChange(
            @ForAll @Size(max = 100) List<@AlphaChars @StringLength(min = 0, max = 20) String> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<String> state = scope.state("", (a, b) -> a.length() == b.length());

            int[] runs = {0};

            scope.effect(() -> {
                state.value();
                runs[0]++;
            });

            String expectedValue = "";
            int expectedRuns = 1;

            for (String value : values) {
                boolean equalByLength = expectedValue.length() == value.length();

                state.set(value);

                if (!equalByLength) {
                    expectedValue = value;
                    expectedRuns++;
                }

                assertEquals(expectedValue, state.value());
                assertEquals(expectedRuns, runs[0]);
            }
        }
    }
}

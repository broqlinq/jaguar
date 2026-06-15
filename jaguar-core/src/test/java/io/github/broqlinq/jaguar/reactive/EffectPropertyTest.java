package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.signal.State;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

final class EffectPropertyTest {

    @Property
    void effectObservesLatestStateAfterEachDistinctChange(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> state = scope.state(initial, Objects::equals);

            int[] observed = {0};
            int[] runs = {0};

            scope.effect(() -> {
                observed[0] = state.value();
                runs[0]++;
            });

            int expected = initial;
            int expectedRuns = 1;

            assertEquals(expected, observed[0]);
            assertEquals(expectedRuns, runs[0]);

            for (int value : values) {
                boolean changed = expected != value;

                state.set(value);

                if (changed) {
                    expected = value;
                    expectedRuns++;
                }

                assertEquals(expected, observed[0]);
                assertEquals(expectedRuns, runs[0]);
            }
        }
    }

    @Property
    void disposedEffectDoesNotObserveLaterChanges(
            @ForAll int initial,
            @ForAll @Size(min = 1, max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> state = scope.state(initial, Objects::equals);

            int[] runs = {0};

            var effect = scope.effect(() -> {
                state.value();
                runs[0]++;
            });

            assertEquals(1, runs[0]);

            effect.dispose();

            for (int value : values) {
                state.set(value);
            }

            assertEquals(1, runs[0]);
        }
    }

    @Property
    void effectTracksMultipleStates(
            @ForAll int firstInitial,
            @ForAll int secondInitial,
            @ForAll("multiStateOperationList") List<MultiStateSet> operations
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Integer> first = scope.state(firstInitial, Objects::equals);
            State<Integer> second = scope.state(secondInitial, Objects::equals);

            int[] observedSum = {0};
            int[] runs = {0};

            scope.effect(() -> {
                observedSum[0] = first.value() + second.value();
                runs[0]++;
            });

            int firstModel = firstInitial;
            int secondModel = secondInitial;
            int expectedRuns = 1;

            assertEquals(firstModel + secondModel, observedSum[0]);

            for (MultiStateSet operation : operations) {
                boolean changed;

                if (operation.target() == Target.FIRST) {
                    changed = firstModel != operation.value();
                    first.set(operation.value());
                    if (changed) {
                        firstModel = operation.value();
                    }
                } else {
                    changed = secondModel != operation.value();
                    second.set(operation.value());
                    if (changed) {
                        secondModel = operation.value();
                    }
                }

                if (changed) {
                    expectedRuns++;
                }

                assertEquals(firstModel + secondModel, observedSum[0]);
                assertEquals(expectedRuns, runs[0]);
            }
        }
    }

    enum Target {
        FIRST,
        SECOND
    }

    record MultiStateSet(Target target, int value) {
    }

    @Provide
    Arbitrary<List<MultiStateSet>> multiStateOperationList() {
        Arbitrary<MultiStateSet> op = Combinators.combine(
                Arbitraries.of(Target.values()),
                Arbitraries.integers().between(-1000, 1000)
        ).as(MultiStateSet::new);

        return op.list().ofMaxSize(100);
    }
}

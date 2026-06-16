package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.Size;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class PrimitiveEffectPropertyTest {

    @Property
    void intEffectObservesLatestDistinctState(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState state = scope.state(initial);

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
    void booleanEffectObservesLatestDistinctState(
            @ForAll boolean initial,
            @ForAll @Size(max = 100) List<Boolean> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanState state = scope.state(initial);

            boolean[] observed = {false};
            int[] runs = {0};

            scope.effect(() -> {
                observed[0] = state.value();
                runs[0]++;
            });

            boolean expected = initial;
            int expectedRuns = 1;

            assertEquals(expected, observed[0]);
            assertEquals(expectedRuns, runs[0]);

            for (boolean value : values) {
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
    void effectTracksMultiplePrimitiveStates(
            @ForAll @IntRange(min = -1000, max = 1000) int initialInt,
            @ForAll @LongRange(min = -1000, max = 1000) long initialLong,
            @ForAll("primitiveOperationLists") List<PrimitiveOperation> operations
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState integer = scope.state(initialInt);
            LongState lng = scope.state(initialLong);

            long[] observed = {0L};
            int[] runs = {0};

            scope.effect(() -> {
                observed[0] = integer.value() + lng.value();
                runs[0]++;
            });

            int modelInt = initialInt;
            long modelLong = initialLong;
            int expectedRuns = 1;

            assertEquals(modelInt + modelLong, observed[0]);
            assertEquals(expectedRuns, runs[0]);

            for (PrimitiveOperation operation : operations) {
                boolean changed;

                if (operation.target() == PrimitiveTarget.INT) {
                    changed = modelInt != operation.intValue();
                    integer.set(operation.intValue());
                    if (changed) {
                        modelInt = operation.intValue();
                    }
                } else {
                    changed = modelLong != operation.longValue();
                    lng.set(operation.longValue());
                    if (changed) {
                        modelLong = operation.longValue();
                    }
                }

                if (changed) {
                    expectedRuns++;
                }

                assertEquals(modelInt + modelLong, observed[0]);
                assertEquals(expectedRuns, runs[0]);
            }
        }
    }

    enum PrimitiveTarget {
        INT,
        LONG
    }

    record PrimitiveOperation(PrimitiveTarget target, int intValue, long longValue) {
    }

    @Provide
    Arbitrary<List<PrimitiveOperation>> primitiveOperationLists() {
        Arbitrary<PrimitiveOperation> operation = Arbitraries.oneOf(
                Arbitraries.integers().between(-1000, 1000)
                        .map(value -> new PrimitiveOperation(PrimitiveTarget.INT, value, 0L)),
                Arbitraries.longs().between(-1000L, 1000L)
                        .map(value -> new PrimitiveOperation(PrimitiveTarget.LONG, 0, value))
        );

        return operation.list().ofMaxSize(100);
    }
}

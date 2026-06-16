package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class PrimitiveDerivedSignalPropertyTest {

    @Property
    void intDerivedSignalMatchesPureMapping(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState source = scope.state(initial);

            IntSignal derived = source.deriveInt(value -> value * 2 + 1);

            assertEquals(initial * 2 + 1, derived.value());

            for (int value : values) {
                source.set(value);

                assertEquals(value * 2 + 1, derived.value());
            }
        }
    }

    @Property
    void intDerivedChainMatchesModel(
            @ForAll int initial,
            @ForAll @IntRange(min = 1, max = 20) int depth,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState source = scope.state(initial);

            IntSignal current = source;

            for (int i = 0; i < depth; i++) {
                IntSignal previous = current;
                current = previous.deriveInt(value -> value + 1);
            }

            IntSignal terminal = current;

            assertEquals(initial + depth, terminal.value());

            for (int value : values) {
                source.set(value);

                assertEquals(value + depth, terminal.value());
            }
        }
    }

    @Property
    void primitiveComputedEqualitySuppressesEffectRuns(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState source = scope.state(initial);

            IntSignal parity = source.deriveInt(value -> Math.floorMod(value, 2));

            int[] observed = {-1};
            int[] runs = {0};

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
                    currentParity = nextParity;
                    expectedRuns++;
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
    void booleanDerivedSignalMatchesModel(
            @ForAll boolean initial,
            @ForAll @Size(max = 100) List<Boolean> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanState source = scope.state(initial);

            BooleanSignal inverted = source.deriveBoolean(value -> !value);
            IntSignal numeric = inverted.deriveInt(value -> value ? 1 : 0);

            assertEquals(!initial, inverted.value());
            assertEquals(!initial ? 1 : 0, numeric.value());

            for (boolean value : values) {
                source.set(value);

                assertEquals(!value, inverted.value());
                assertEquals(!value ? 1 : 0, numeric.value());
            }
        }
    }

    @Property
    void longDerivedSignalMatchesPureMapping(
            @ForAll long initial,
            @ForAll @Size(max = 100) List<Long> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            LongState source = scope.state(initial);

            LongSignal derived = source.deriveLong(value -> value * 2L + 1L);

            assertEquals(initial * 2L + 1L, derived.value());

            for (long value : values) {
                source.set(value);

                assertEquals(value * 2L + 1L, derived.value());
            }
        }
    }

    @Property
    void doubleDerivedSignalMatchesPureMapping(
            @ForAll("finiteDoubles") double initial,
            @ForAll("finiteDoubleLists") List<Double> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            DoubleState source = scope.state(initial);

            DoubleSignal derived = source.deriveDouble(value -> value * 2.0d + 1.0d);

            assertEquals(initial * 2.0d + 1.0d, derived.value());

            for (double value : values) {
                source.set(value);

                assertEquals(value * 2.0d + 1.0d, derived.value());
            }
        }
    }

    @Provide
    Arbitrary<Double> finiteDoubles() {
        return Arbitraries.doubles()
                .between(-1_000_000.0d, 1_000_000.0d)
                .filter(Double::isFinite);
    }

    @Provide
    Arbitrary<List<Double>> finiteDoubleLists() {
        return finiteDoubles().list().ofMaxSize(100);
    }
}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class PrimitiveStatePropertyTest {

    @Property
    void intStateReflectsLastSet(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<Integer> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState state = scope.state(initial);

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
    void intStateUpdateMatchesModel(
            @ForAll int initial,
            @ForAll @Size(max = 100) List<@IntRange(min = -1000, max = 1000) Integer> deltas
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState state = scope.state(initial);

            int expected = initial;

            for (int delta : deltas) {
                state.update(value -> value + delta);
                expected += delta;

                assertEquals(expected, state.value());
            }
        }
    }

    @Property
    void booleanStateSetAndUpdateMatchesModel(
            @ForAll boolean initial,
            @ForAll @Size(max = 100) List<BooleanOperation> operations
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanState state = scope.state(initial);

            boolean expected = initial;

            for (BooleanOperation operation : operations) {
                switch (operation) {
                    case SET_TRUE -> {
                        state.set(true);
                        expected = true;
                    }
                    case SET_FALSE -> {
                        state.set(false);
                        expected = false;
                    }
                    case TOGGLE -> {
                        state.update(value -> !value);
                        expected = !expected;
                    }
                }

                assertEquals(expected, state.value());
            }
        }
    }

    @Property
    void longStateReflectsLastSet(
            @ForAll long initial,
            @ForAll @Size(max = 100) List<Long> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            LongState state = scope.state(initial);

            long expected = initial;

            for (long value : values) {
                state.set(value);
                expected = value;

                assertEquals(expected, state.value());
            }
        }
    }

    @Property
    void floatStateReflectsLastFiniteSet(
            @ForAll("finiteFloats") float initial,
            @ForAll("finiteFloatLists") List<Float> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            FloatState state = scope.state(initial);

            float expected = initial;

            for (float value : values) {
                state.set(value);
                expected = value;

                assertEquals(expected, state.value());
            }
        }
    }

    @Property
    void doubleStateReflectsLastFiniteSet(
            @ForAll("finiteDoubles") double initial,
            @ForAll("finiteDoubleLists") List<Double> values
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            DoubleState state = scope.state(initial);

            double expected = initial;

            for (double value : values) {
                state.set(value);
                expected = value;

                assertEquals(expected, state.value());
            }
        }
    }

    enum BooleanOperation {
        SET_TRUE,
        SET_FALSE,
        TOGGLE
    }

    @Provide
    Arbitrary<Float> finiteFloats() {
        return Arbitraries.floats()
                .between(-1_000_000.0f, 1_000_000.0f)
                .filter(Float::isFinite);
    }

    @Provide
    Arbitrary<List<Float>> finiteFloatLists() {
        return finiteFloats().list().ofMaxSize(100);
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

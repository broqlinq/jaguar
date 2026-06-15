package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PrimitiveStateSignalTest {

    @Test
    void booleanStateSetAndUpdate() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanState state = scope.state(false);

            assertFalse(state.value());

            state.set(true);
            assertTrue(state.value());

            state.update(value -> !value);
            assertFalse(state.value());
        }
    }

    @Test
    void intStateSetAndUpdate() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState state = scope.state(1);

            assertEquals(1, state.value());

            state.set(10);
            assertEquals(10, state.value());

            state.update(value -> value + 5);
            assertEquals(15, state.value());
        }
    }

    @Test
    void longStateSetAndUpdate() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            LongState state = scope.state(1L);

            assertEquals(1L, state.value());

            state.set(10L);
            assertEquals(10L, state.value());

            state.update(value -> value + 5L);
            assertEquals(15L, state.value());
        }
    }

    @Test
    void floatStateSetAndUpdate() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            FloatState state = scope.state(1.0f);

            assertEquals(1.0f, state.value());

            state.set(10.0f);
            assertEquals(10.0f, state.value());

            state.update(value -> value + 5.0f);
            assertEquals(15.0f, state.value());
        }
    }

    @Test
    void doubleStateSetAndUpdate() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            DoubleState state = scope.state(1.0d);

            assertEquals(1.0d, state.value());

            state.set(10.0d);
            assertEquals(10.0d, state.value());

            state.update(value -> value + 5.0d);
            assertEquals(15.0d, state.value());
        }
    }

    @Test
    void settingSamePrimitiveValueDoesNotRerunEffect() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState state = scope.state(1);

            int[] runs = {0};

            scope.effect(() -> {
                state.value();
                runs[0]++;
            });

            assertEquals(1, runs[0]);

            state.set(1);

            assertEquals(1, runs[0]);

            state.set(2);

            assertEquals(2, runs[0]);
        }
    }

    @Test
    void primitiveEffectObservesLatestValues() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanState enabled = scope.state(false);
            IntState count = scope.state(1);
            LongState total = scope.state(10L);
            FloatState ratio = scope.state(1.5f);
            DoubleState precise = scope.state(2.5d);

            int[] runs = {0};
            Object[] observed = new Object[5];

            scope.effect(() -> {
                observed[0] = enabled.value();
                observed[1] = count.value();
                observed[2] = total.value();
                observed[3] = ratio.value();
                observed[4] = precise.value();
                runs[0]++;
            });

            assertEquals(1, runs[0]);
            assertEquals(false, observed[0]);
            assertEquals(1, observed[1]);
            assertEquals(10L, observed[2]);
            assertEquals(1.5f, observed[3]);
            assertEquals(2.5d, observed[4]);

            enabled.set(true);
            count.set(2);
            total.set(20L);
            ratio.set(3.5f);
            precise.set(4.5d);

            assertEquals(6, runs[0]);
            assertEquals(true, observed[0]);
            assertEquals(2, observed[1]);
            assertEquals(20L, observed[2]);
            assertEquals(3.5f, observed[3]);
            assertEquals(4.5d, observed[4]);
        }
    }
}

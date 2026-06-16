package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class PrimitiveDerivedSignalTest {

    @Test
    void intStateDerivesAllSignalKinds() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState source = scope.state(10);

            Signal<String> object = source.derive(value -> "v=" + value);
            BooleanSignal bool = source.deriveBoolean(value -> value > 5);
            IntSignal integer = source.deriveInt(value -> value + 1);
            LongSignal lng = source.deriveLong(value -> value + 2L);
            FloatSignal flt = source.deriveFloat(value -> value + 3.0f);
            DoubleSignal dbl = source.deriveDouble(value -> value + 4.0d);

            assertEquals("v=10", object.value());
            assertTrue(bool.value());
            assertEquals(11, integer.value());
            assertEquals(12L, lng.value());
            assertEquals(13.0f, flt.value());
            assertEquals(14.0d, dbl.value());

            source.set(20);

            assertEquals("v=20", object.value());
            assertTrue(bool.value());
            assertEquals(21, integer.value());
            assertEquals(22L, lng.value());
            assertEquals(23.0f, flt.value());
            assertEquals(24.0d, dbl.value());
        }
    }

    @Test
    void booleanStateDerivesAllSignalKinds() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanState source = scope.state(false);

            Signal<String> object = source.derive(value -> value ? "true" : "false");
            BooleanSignal bool = source.deriveBoolean(value -> !value);
            IntSignal integer = source.deriveInt(value -> value ? 1 : 0);
            LongSignal lng = source.deriveLong(value -> value ? 2L : 0L);
            FloatSignal flt = source.deriveFloat(value -> value ? 3.0f : 0.0f);
            DoubleSignal dbl = source.deriveDouble(value -> value ? 4.0d : 0.0d);

            assertEquals("false", object.value());
            assertTrue(bool.value());
            assertEquals(0, integer.value());
            assertEquals(0L, lng.value());
            assertEquals(0.0f, flt.value());
            assertEquals(0.0d, dbl.value());

            source.set(true);

            assertEquals("true", object.value());
            assertFalse(bool.value());
            assertEquals(1, integer.value());
            assertEquals(2L, lng.value());
            assertEquals(3.0f, flt.value());
            assertEquals(4.0d, dbl.value());
        }
    }

    @Test
    void derivedPrimitiveSignalCachesBetweenSourceChanges() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState source = scope.state(1);

            var calls = new AtomicInteger();

            IntSignal derived = source.deriveInt(value -> {
                calls.incrementAndGet();
                return value * 2;
            });

            assertEquals(0, calls.get());

            assertEquals(2, derived.value());
            assertEquals(2, derived.value());
            assertEquals(2, derived.value());

            assertEquals(1, calls.get());

            source.set(2);

            assertEquals(4, derived.value());
            assertEquals(4, derived.value());

            assertEquals(2, calls.get());
        }
    }

    @Test
    void primitiveComputedEqualitySuppressesEffectRun() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState source = scope.state(1);

            IntSignal parity = source.deriveInt(value -> Math.floorMod(value, 2));

            int[] runs = {0};
            int[] observed = {-1};

            scope.effect(() -> {
                observed[0] = parity.value();
                runs[0]++;
            });

            assertEquals(1, runs[0]);
            assertEquals(1, observed[0]);

            source.set(3);

            assertEquals(1, runs[0], "parity stayed equal, effect should skip");

            source.set(4);

            assertEquals(2, runs[0]);
            assertEquals(0, observed[0]);
        }
    }

    @Test
    void primitiveDerivedChainUpdatesCorrectly() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntState source = scope.state(1);

            IntSignal plusOne = source.deriveInt(value -> value + 1);
            IntSignal timesTwo = plusOne.deriveInt(value -> value * 2);
            Signal<String> label = timesTwo.derive(value -> "v=" + value);

            assertEquals("v=4", label.value());

            source.set(2);

            assertEquals("v=6", label.value());
        }
    }
}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class PrimitiveConstantSignalTest {

    @Test
    void primitiveConstantsReturnStoredValues() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanSignal bool = scope.constant(true);
            IntSignal integer = scope.constant(42);
            LongSignal lng = scope.constant(42L);
            FloatSignal flt = scope.constant(1.5f);
            DoubleSignal dbl = scope.constant(2.5d);

            assertTrue(bool.value());
            assertEquals(42, integer.value());
            assertEquals(42L, lng.value());
            assertEquals(1.5f, flt.value());
            assertEquals(2.5d, dbl.value());
        }
    }

    @Test
    void intConstantDerivesEagerConstants() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntSignal source = scope.constant(10);

            Signal<String> object = source.derive(value -> "v=" + value);
            BooleanSignal bool = source.deriveBoolean(value -> value > 0);
            IntSignal integer = source.deriveInt(value -> value + 1);
            LongSignal lng = source.deriveLong(value -> value + 2L);
            FloatSignal flt = source.deriveFloat(value -> value + 3.5f);
            DoubleSignal dbl = source.deriveDouble(value -> value + 4.5d);

            assertEquals("v=10", object.value());
            assertTrue(bool.value());
            assertEquals(11, integer.value());
            assertEquals(12L, lng.value());
            assertEquals(13.5f, flt.value());
            assertEquals(14.5d, dbl.value());
        }
    }

    @Test
    void booleanConstantDerivesEagerConstants() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            BooleanSignal source = scope.constant(true);

            Signal<String> object = source.derive(value -> value ? "yes" : "no");
            BooleanSignal bool = source.deriveBoolean(value -> !value);
            IntSignal integer = source.deriveInt(value -> value ? 1 : 0);
            LongSignal lng = source.deriveLong(value -> value ? 2L : 0L);
            FloatSignal flt = source.deriveFloat(value -> value ? 3.0f : 0.0f);
            DoubleSignal dbl = source.deriveDouble(value -> value ? 4.0d : 0.0d);

            assertEquals("yes", object.value());
            assertFalse(bool.value());
            assertEquals(1, integer.value());
            assertEquals(2L, lng.value());
            assertEquals(3.0f, flt.value());
            assertEquals(4.0d, dbl.value());
        }
    }

    @Test
    void primitiveConstantDeriveRunsMapperOnlyOnce() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            IntSignal source = scope.constant(10);
            var calls = new AtomicInteger();

            IntSignal derived = source.deriveInt(value -> {
                calls.incrementAndGet();
                return value + 1;
            });

            assertEquals(1, calls.get());

            assertEquals(11, derived.value());
            assertEquals(11, derived.value());
            assertEquals(11, derived.value());

            assertEquals(1, calls.get());
        }
    }
}

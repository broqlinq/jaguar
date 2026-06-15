package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.signal.Signal;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class ConstantSignalTest {

    @Test
    void constantReturnsStoredValue() {
        try (var scope = ReactiveTestSupport.newScope()) {
            Signal<String> signal = scope.constant("hello");

            assertEquals("hello", signal.value());
            assertEquals("hello", signal.value());
        }
    }

    @Test
    void derivedConstantIsEvaluatedEagerlyOnce() {
        try (var scope = ReactiveTestSupport.newScope()) {
            var calls = new AtomicInteger();

            Signal<String> source = scope.constant("hello");

            Signal<Integer> length = source.derive(value -> {
                calls.incrementAndGet();
                return value.length();
            });

            assertEquals(1, calls.get());

            assertEquals(5, length.value());
            assertEquals(5, length.value());

            assertEquals(1, calls.get());
        }
    }

    @Test
    void constantDeriveRejectsNullFunction() {
        try (var scope = ReactiveTestSupport.newScope()) {
            Signal<String> source = scope.constant("hello");

            assertThrows(NullPointerException.class, () -> source.derive(null));
            assertThrows(NullPointerException.class, () -> source.deriveBoolean(null));
            assertThrows(NullPointerException.class, () -> source.deriveInt(null));
            assertThrows(NullPointerException.class, () -> source.deriveLong(null));
            assertThrows(NullPointerException.class, () -> source.deriveFloat(null));
            assertThrows(NullPointerException.class, () -> source.deriveDouble(null));
        }
    }

    @Test
    void derivedConstantCanContainNull() {
        try (var scope = ReactiveTestSupport.newScope()) {
            Signal<String> source = scope.constant("hello");

            Signal<Object> derived = source.derive(value -> null);

            assertNull(derived.value());
        }
    }

    @Test
    void constantUsesScopeReadChecks() {
        Signal<String> signal;

        var scope = ReactiveTestSupport.newScope();
        signal = scope.constant("hello");
        scope.close();

        assertThrows(IllegalStateException.class, signal::value);
    }
}

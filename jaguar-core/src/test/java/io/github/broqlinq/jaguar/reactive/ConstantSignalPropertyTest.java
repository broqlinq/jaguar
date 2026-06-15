package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.signal.Signal;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

final class ConstantSignalPropertyTest {

    @Property
    void constantAlwaysReturnsOriginalValue(@ForAll String value, @ForAll @IntRange(min = 1, max = 100) int reads) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            Signal<String> constant = scope.constant(value);

            for (int i = 0; i < reads; i++) {
                assertEquals(value, constant.value());
            }
        }
    }

    @Property
    void derivedConstantIsEagerAndStable(@ForAll String value, @ForAll @IntRange(min = 1, max = 100) int reads) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            var calls = new AtomicInteger();

            Signal<String> constant = scope.constant(value);
            Signal<Integer> derived = constant.derive(v -> {
                calls.incrementAndGet();
                return v.length();
            });

            assertEquals(1, calls.get());

            for (int i = 0; i < reads; i++) {
                assertEquals(value.length(), derived.value());
            }

            assertEquals(1, calls.get());
        }
    }

    @Property
    void derivedConstantUsesObjectEqualityByValue(@ForAll String value) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            Signal<String> constant = scope.constant(value);

            Signal<String> derived = constant.derive(String::new);

            assertEquals(value, derived.value());
            assertTrue(Objects.equals(value, derived.value()));
        }
    }
}

package io.github.broqlinq.jaguar.reactive;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ScopeLifecycleTest {

    @Test
    void closingScopeMakesConstantUnreadable() {
        var scope = ReactiveTestSupport.newScope();

        var constant = scope.constant("a");

        scope.close();

        assertThrows(IllegalStateException.class, constant::value);
    }

    @Test
    void closingScopeMakesStateUnreadableAndUnwritable() {
        var scope = ReactiveTestSupport.newScope();

        var state = scope.state("a", Objects::equals);

        scope.close();

        assertThrows(IllegalStateException.class, state::value);
        assertThrows(IllegalStateException.class, () -> state.set("b"));
        assertThrows(IllegalStateException.class, () -> state.update(value -> "b"));
    }

    @Test
    void closingScopeMakesComputedUnreadable() {
        var scope = ReactiveTestSupport.newScope();

        var source = scope.state("a", Objects::equals);
        var computed = scope.computed(() -> source.value().toUpperCase(), Objects::equals);

        scope.close();

        assertThrows(IllegalStateException.class, computed::value);
    }

    @Test
    void closingScopeDisposesEffectBehaviorally() {
        var scope = ReactiveTestSupport.newScope();

        var state = scope.state("a", Objects::equals);
        var runs = new java.util.concurrent.atomic.AtomicInteger();

        scope.effect(() -> {
            state.value();
            runs.incrementAndGet();
        });

        assertEquals(1, runs.get());

        scope.close();

        assertThrows(IllegalStateException.class, () -> state.set("b"));
        assertEquals(1, runs.get());
    }

    @Test
    void cannotCreateConstantAfterScopeClose() {
        var scope = ReactiveTestSupport.newScope();

        scope.close();

        assertThrows(IllegalStateException.class, () -> scope.constant("a"));
    }

    @Test
    void cannotCreateStateAfterScopeClose() {
        var scope = ReactiveTestSupport.newScope();

        scope.close();

        assertThrows(IllegalStateException.class, () -> scope.state("a", Objects::equals));
    }

    @Test
    void cannotCreateComputedAfterScopeClose() {
        var scope = ReactiveTestSupport.newScope();

        scope.close();

        assertThrows(IllegalStateException.class, () -> scope.computed(() -> "a", Objects::equals));
    }

    @Test
    void cannotCreateEffectAfterScopeClose() {
        var scope = ReactiveTestSupport.newScope();

        scope.close();

        assertThrows(IllegalStateException.class, () -> scope.effect(() -> {
        }));
    }
}
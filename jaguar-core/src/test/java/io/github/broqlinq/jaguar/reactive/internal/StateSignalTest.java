package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.signal.State;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;

final class StateSignalTest {

    @Test
    void stateReturnsInitialValue() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("initial", Objects::equals);

            assertEquals("initial", state.value());
        }
    }

    @Test
    void setChangesValue() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            state.set("b");

            assertEquals("b", state.value());
        }
    }

    @Test
    void updateChangesValue() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            state.update(value -> value + "b");

            assertEquals("ab", state.value());
        }
    }

    @Test
    void setSameValueDoesNotNotifyEffect() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();

            scope.effect(() -> {
                state.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            state.set("a");

            assertEquals(1, runs.get());
        }
    }

    @Test
    void updateSameValueDoesNotNotifyEffect() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();

            scope.effect(() -> {
                state.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            state.update(value -> value);

            assertEquals(1, runs.get());
        }
    }

    @Test
    void setDifferentValueNotifiesEffect() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();

            scope.effect(() -> {
                state.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            state.set("b");

            assertEquals(2, runs.get());
        }
    }

    @Test
    void customEqualitySuppressesUpdate() {
        try (var scope = ReactiveTestSupport.newScope()) {
            BiPredicate<String, String> sameLength = (a, b) -> a.length() == b.length();

            State<String> state = scope.state("ab", sameLength);

            var runs = new AtomicInteger();

            scope.effect(() -> {
                state.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            state.set("cd");

            assertEquals("ab", state.value(), "Value should not be replaced when equality says equal");
            assertEquals(1, runs.get());

            state.set("xyz");

            assertEquals("xyz", state.value());
            assertEquals(2, runs.get());
        }
    }

    @Test
    void disposedEffectUnsubscribesFromState() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            var runs = new AtomicInteger();

            var effect = scope.effect(() -> {
                state.value();
                runs.incrementAndGet();
            });

            assertEquals(1, runs.get());

            effect.dispose();

            state.set("b");

            assertEquals(1, runs.get());
        }
    }

    @Test
    void stateRejectsNullUpdater() {
        try (var scope = ReactiveTestSupport.newScope()) {
            State<String> state = scope.state("a", Objects::equals);

            assertThrows(NullPointerException.class, () -> state.update(null));
        }
    }

    @Test
    void stateReadAfterScopeCloseThrows() {
        State<String> state;

        var scope = ReactiveTestSupport.newScope();
        state = scope.state("a", Objects::equals);
        scope.close();

        assertThrows(IllegalStateException.class, state::value);
    }

    @Test
    void stateWriteAfterScopeCloseThrows() {
        State<String> state;

        var scope = ReactiveTestSupport.newScope();
        state = scope.state("a", Objects::equals);
        scope.close();

        assertThrows(IllegalStateException.class, () -> state.set("b"));
    }
}

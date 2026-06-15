package io.github.broqlinq.jaguar.reactive;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

final class ScopeLifecyclePropertyTest {

    @Property
    void closedScopeRejectsReadsAndWrites(@ForAll int initial, @ForAll @Size(max = 20) List<Integer> values) {
        var scope = ReactivePropertyTestSupport.newScope();

        var state = scope.state(initial, Objects::equals);
        var computed = scope.computed(() -> state.value() + 1, Objects::equals);
        var constant = scope.constant((Integer) initial);

        scope.close();

        assertThrows(IllegalStateException.class, state::value);
        assertThrows(IllegalStateException.class, computed::value);
        assertThrows(IllegalStateException.class, constant::value);

        for (int value : values) {
            assertThrows(IllegalStateException.class, () -> state.set(value));
        }
    }

    @Property
    void closedScopeRejectsNewResources(@ForAll int value) {
        var scope = ReactivePropertyTestSupport.newScope();

        scope.close();

        assertThrows(IllegalStateException.class, () -> scope.constant(value));
        assertThrows(IllegalStateException.class, () -> scope.state(value, Objects::equals));
        assertThrows(IllegalStateException.class, () -> scope.computed(() -> value, Objects::equals));
        assertThrows(IllegalStateException.class, () -> scope.effect(() -> {
        }));
    }
}

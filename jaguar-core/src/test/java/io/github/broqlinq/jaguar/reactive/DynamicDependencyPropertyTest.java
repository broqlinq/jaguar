package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.signal.Signal;
import io.github.broqlinq.jaguar.reactive.signal.State;
import net.jqwik.api.*;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

final class DynamicDependencyPropertyTest {

    @Property
    void computedDynamicDependenciesMatchModel(
            @ForAll boolean initialUseA,
            @ForAll int initialA,
            @ForAll int initialB,
            @ForAll("dynamicOperationLists") List<DynamicOperation> operations
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Boolean> useA = scope.state(initialUseA, Objects::equals);
            State<Integer> a = scope.state(initialA, Objects::equals);
            State<Integer> b = scope.state(initialB, Objects::equals);

            Signal<Integer> selected = scope.computed(
                    () -> useA.value() ? a.value() : b.value(),
                    Objects::equals
            );

            boolean modelUseA = initialUseA;
            int modelA = initialA;
            int modelB = initialB;

            assertEquals(modelUseA ? modelA : modelB, selected.value());

            for (DynamicOperation operation : operations) {
                switch (operation.kind()) {
                    case SET_USE_A -> {
                        useA.set(operation.boolValue());
                        modelUseA = operation.boolValue();
                    }
                    case SET_A -> {
                        a.set(operation.intValue());
                        modelA = operation.intValue();
                    }
                    case SET_B -> {
                        b.set(operation.intValue());
                        modelB = operation.intValue();
                    }
                }

                assertEquals(modelUseA ? modelA : modelB, selected.value());
            }
        }
    }

    @Property
    void effectDynamicDependenciesRemoveStaleEdges(
            @ForAll boolean initialUseA,
            @ForAll int initialA,
            @ForAll int initialB,
            @ForAll("dynamicOperationLists") List<DynamicOperation> operations
    ) {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            State<Boolean> useA = scope.state(initialUseA, Objects::equals);
            State<Integer> a = scope.state(initialA, Objects::equals);
            State<Integer> b = scope.state(initialB, Objects::equals);

            int[] observed = {0};
            int[] runs = {0};

            scope.effect(() -> {
                observed[0] = useA.value() ? a.value() : b.value();
                runs[0]++;
            });

            boolean modelUseA = initialUseA;
            int modelA = initialA;
            int modelB = initialB;
            int modelObserved = modelUseA ? modelA : modelB;
            int expectedRuns = 1;

            assertEquals(modelObserved, observed[0]);
            assertEquals(expectedRuns, runs[0]);

            for (DynamicOperation operation : operations) {
                boolean oldUseA = modelUseA;
                int oldA = modelA;
                int oldB = modelB;
                int oldObserved = modelObserved;

                switch (operation.kind()) {
                    case SET_USE_A -> {
                        useA.set(operation.boolValue());
                        modelUseA = operation.boolValue();
                    }
                    case SET_A -> {
                        a.set(operation.intValue());
                        modelA = operation.intValue();
                    }
                    case SET_B -> {
                        b.set(operation.intValue());
                        modelB = operation.intValue();
                    }
                }

                modelObserved = modelUseA ? modelA : modelB;

                boolean relevantDependencyChanged = switch (operation.kind()) {
                    case SET_USE_A -> oldUseA != modelUseA;
                    case SET_A -> oldUseA && oldA != modelA;
                    case SET_B -> !oldUseA && oldB != modelB;
                };

                if (relevantDependencyChanged) {
                    expectedRuns++;
                }

                assertEquals(modelObserved, observed[0]);
                assertEquals(expectedRuns, runs[0]);
            }
        }
    }

    enum DynamicKind {
        SET_USE_A,
        SET_A,
        SET_B
    }

    record DynamicOperation(DynamicKind kind, boolean boolValue, int intValue) {
    }

    @Provide
    Arbitrary<List<DynamicOperation>> dynamicOperationLists() {
        Arbitrary<DynamicOperation> operation = Arbitraries.oneOf(
                Arbitraries.integers().between(1, 10).map(value -> new DynamicOperation(DynamicKind.SET_USE_A, value % 2 == 0, 0)),
                Arbitraries.integers().between(-1000, 1000).map(value -> new DynamicOperation(DynamicKind.SET_A, false, value)),
                Arbitraries.integers().between(-1000, 1000).map(value -> new DynamicOperation(DynamicKind.SET_B, false, value))
        );

        return operation.list().ofMaxSize(100);
    }
}

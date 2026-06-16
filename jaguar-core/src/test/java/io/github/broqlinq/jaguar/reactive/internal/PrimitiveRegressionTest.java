package io.github.broqlinq.jaguar.reactive.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PrimitiveRegressionTest {

    @Test
    void firstReadOfUninitializedPrimitiveComputedInsideEffectDoesNotScheduleSecondRun() {
        try (var scope = ReactivePropertyTestSupport.newScope()) {
            var source = scope.state(1);

            var computed = source.deriveInt(value -> value + 1);

            int[] runs = {0};

            scope.effect(() -> {
                computed.value();
                runs[0]++;
            });

            assertEquals(1, runs[0]);
        }
    }
}

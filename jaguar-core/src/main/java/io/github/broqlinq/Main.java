package io.github.broqlinq;

import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.SchedulerImpl;
import io.github.broqlinq.jaguar.reactive.signal.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static void main() {
//        closingScopeDisposesEffectBehaviorally();
//        computedEqualitySuppressesDownstreamEffectRun();
        primitiveEffectObservesLatestValues();
    }

    private static Scope newScope() {
        return new ConfinedScope(new SchedulerImpl(Thread.currentThread()));
    }

    static void primitiveEffectObservesLatestValues() {
        try (var scope = newScope()) {
            BooleanState enabled = scope.state(false);
            IntState count = scope.state(1);
            LongState total = scope.state(10L);
            FloatState ratio = scope.state(1.5f);
            DoubleState precise = scope.state(2.5d);

            final int[] runs = {0};
            final Object[] observed = new Object[5];

            scope.effect(() -> {
                observed[0] = enabled.value();
                observed[1] = count.value();
                observed[2] = total.value();
                observed[3] = ratio.value();
                observed[4] = precise.value();
                runs[0]++;
            });

//            assertEquals(1, runs[0]);
//            assertEquals(false, observed[0]);
//            assertEquals(1, observed[1]);
//            assertEquals(10L, observed[2]);
//            assertEquals(1.5f, observed[3]);
//            assertEquals(2.5d, observed[4]);

            IO.println(runs[0]);
            IO.println(Arrays.toString(observed));

            enabled.set(true);
            count.set(2);
            total.set(20L);
            ratio.set(3.5f);
            precise.set(4.5d);

            IO.println(runs[0]);
            IO.println(Arrays.toString(observed));

//            assertEquals(6, runs[0]);
//            assertEquals(true, observed[0]);
//            assertEquals(2, observed[1]);
//            assertEquals(20L, observed[2]);
//            assertEquals(3.5f, observed[3]);
//            assertEquals(4.5d, observed[4]);
        }
    }

    static void closingScopeDisposesEffectBehaviorally() {
        var scope = newScope();

        var state = scope.state("a", Objects::equals);
        var runs = new java.util.concurrent.atomic.AtomicInteger();

        scope.effect(() -> {
            state.value();
            runs.incrementAndGet();
        });

        var value = runs.get();
        IO.println("1 == " + value + ": " + (value == 1));

        scope.close();
    }

    static void computedEqualitySuppressesDownstreamEffectRun() {
        try (var scope = newScope()) {
            State<Integer> count = scope.state(1, Objects::equals);

            Signal<Integer> parity = scope.computed(
                    () -> count.value() % 2,
                    Objects::equals
            );

            var runs = new AtomicInteger();

            scope.effect(() -> {
                parity.value();
                runs.incrementAndGet();
            });

            IO.println("Runs: " + runs.get());

            count.set(3);

            IO.println("Runs: " + runs.get());

            count.set(4);

            IO.println("Runs: " + runs.get());
        }
    }
}
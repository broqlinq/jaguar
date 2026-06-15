package io.github.broqlinq;

import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.SchedulerImpl;
import io.github.broqlinq.jaguar.reactive.signal.Signal;
import io.github.broqlinq.jaguar.reactive.signal.State;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static void main() {
//        closingScopeDisposesEffectBehaviorally();
        computedEqualitySuppressesDownstreamEffectRun();
    }

    private static Scope newScope() {
        return new ConfinedScope(new SchedulerImpl(Thread.currentThread()));
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
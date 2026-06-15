package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.SchedulerImpl;

final class ReactiveTestSupport {

    private ReactiveTestSupport() {
    }

    static ConfinedScope newScope() {
        /*
         * Adjust this to your current construction API.
         *
         * Examples:
         *   return new ConfinedScope("test", new ThreadConfinedScheduler(Thread.currentThread()));
         *   return new ConfinedScope(new DirectScheduler());
         *   return ConfinedScope.create("test");
         */
        return new ConfinedScope(new SchedulerImpl(Thread.currentThread()));
    }
}
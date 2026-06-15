package io.github.broqlinq.jaguar.reactive.internal;

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
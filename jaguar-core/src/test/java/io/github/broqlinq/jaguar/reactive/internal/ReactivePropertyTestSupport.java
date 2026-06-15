package io.github.broqlinq.jaguar.reactive.internal;

final class ReactivePropertyTestSupport {

    private ReactivePropertyTestSupport() {
    }

    static ConfinedScope newScope() {
        return new ConfinedScope(new SchedulerImpl(Thread.currentThread()));
    }
}

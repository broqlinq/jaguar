package io.github.broqlinq.jaguar.reactive;

import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.SchedulerImpl;

final class ReactivePropertyTestSupport {

    private ReactivePropertyTestSupport() {
    }

    static ConfinedScope newScope() {
        return new ConfinedScope(new SchedulerImpl(Thread.currentThread()));
    }
}

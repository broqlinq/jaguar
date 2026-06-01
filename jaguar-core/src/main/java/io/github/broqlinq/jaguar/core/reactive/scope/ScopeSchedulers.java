package io.github.broqlinq.jaguar.core.reactive.scope;

import io.github.broqlinq.jaguar.core.reactive.scope.internal.ImmediateScheduler;

public final class ScopeSchedulers {

    private ScopeSchedulers() {}

    public static ReactiveScope.Scheduler immediateScheduler(Thread owner) {
        return new ImmediateScheduler(owner);
    }
}

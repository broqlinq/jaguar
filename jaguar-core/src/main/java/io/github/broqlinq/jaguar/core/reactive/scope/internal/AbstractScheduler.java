package io.github.broqlinq.jaguar.core.reactive.scope.internal;

import io.github.broqlinq.jaguar.core.reactive.scope.ReactiveScope;

import java.util.Objects;

public abstract class AbstractScheduler implements ReactiveScope.Scheduler {

    protected final Thread owner;

    protected AbstractScheduler(Thread owner) {
        this.owner = Objects.requireNonNull(owner);
    }

    @Override
    public boolean isOwnerThread() {
        return Thread.currentThread() == owner;
    }
}

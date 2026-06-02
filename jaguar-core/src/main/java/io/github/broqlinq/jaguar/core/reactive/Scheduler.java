package io.github.broqlinq.jaguar.core.reactive;

public interface Scheduler {

    boolean isOwnerThread();

    void schedule(Runnable task);

    void batch(Runnable task);
}

package io.github.broqlinq.jaguar.reactive;

public interface Scheduler {

    boolean isOwnerThread();

    void schedule(Runnable task);

    void batch(Runnable task);
}

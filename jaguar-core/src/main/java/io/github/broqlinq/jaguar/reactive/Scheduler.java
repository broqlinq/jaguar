package io.github.broqlinq.jaguar.reactive;

public interface Scheduler {

    void schedule(Runnable task);

    void process(Runnable batch);
}

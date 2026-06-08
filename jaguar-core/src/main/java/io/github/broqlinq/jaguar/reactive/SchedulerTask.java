package io.github.broqlinq.jaguar.reactive;

public interface SchedulerTask extends Runnable {

    void execute();

    default boolean isQueued() {
        return false;
    }

    @Override
    default void run() {
        execute();
    }
}

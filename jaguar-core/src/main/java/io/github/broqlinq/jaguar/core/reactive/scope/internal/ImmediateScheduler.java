package io.github.broqlinq.jaguar.core.reactive.scope.internal;

public class ImmediateScheduler extends AbstractScheduler {

    public ImmediateScheduler(Thread owner) {
        super(owner);
    }

    @Override
    public void schedule(Runnable task) {
        if (isOwnerThread()) {
            task.run();
        } else {
            throw new WrongThreadException();
        }
    }

    @Override
    public void batch(Runnable batchTask) {
        if (isOwnerThread()) {
            batchTask.run();
        } else {
            throw new WrongThreadException();
        }
    }
}

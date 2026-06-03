package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scheduler;

import java.util.ArrayDeque;

public final class ThreadConfinedScheduler implements Scheduler {

    private final Thread ownerThread;
    private final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
    private int batchDepth = 0;
    private boolean flushing = false;

    public ThreadConfinedScheduler(Thread ownerThread) {
        this.ownerThread = ownerThread;
    }

    @Override
    public boolean isOwnerThread() {
        return Thread.currentThread() == ownerThread;
    }

    @Override
    public void schedule(Runnable task) {
        assertOwnerThread("Attempted to schedule task on non-owner thread");

        tasks.offerLast(task);

        if (batchDepth == 0) {
            flush();
        }
    }

    @Override
    public void batch(Runnable task) {
        assertOwnerThread("Attempted to batch task on non-owner thread");

        batchDepth++;

        boolean completed = false;
        try {
            task.run();
            completed = true;
        } finally {
            batchDepth--;
            if (batchDepth == 0 && completed) {
                flush();
            }
        }
    }

    private void assertOwnerThread(String message) {
        if (!isOwnerThread()) {
            throw new WrongThreadException(message);
        }
    }

    private void flush() {
        if (flushing) {
            return;
        }

        flushing = true;
        try {
            Runnable task;
            while ((task = tasks.pollFirst()) != null) {
                task.run();
            }
        } finally {
            flushing = false;
        }
    }
}

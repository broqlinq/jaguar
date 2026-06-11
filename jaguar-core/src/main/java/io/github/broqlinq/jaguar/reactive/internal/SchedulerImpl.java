package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scheduler;

import static java.util.Objects.requireNonNull;

public final class SchedulerImpl implements Scheduler {

    private static final int MAX_CAPACITY = 0x1 << 14;
    private static final int MASK = MAX_CAPACITY - 1;

    private final Thread ownerThread;

    private final Runnable[] scheduledTasks = new Runnable[MAX_CAPACITY];
    private int head = 0;
    private int tail = 0;

    private int batchLevel = 0;
    private boolean flushing = false;

    public SchedulerImpl(Thread ownerThread) {
        requireNonNull(ownerThread, "owner thread cannot be null");
        this.ownerThread = ownerThread;
    }

    private static int masked(int i) {
        return i & MASK;
    }

    @Override
    public void schedule(Runnable task) {
        assertOwnerThread();
        requireNonNull(task, "task");

        scheduleUnchecked(task);
    }

    public void scheduleUnchecked(Runnable task) {
        enqueue(task);

        if (notBatching()) {
            flush();
        }
    }

    private void enqueue(Runnable task) {
        final int capacity = scheduledTasks.length;
        final int head = this.head;
        final int tail = this.tail;
        if (head - capacity >= tail) {
            throw new IllegalStateException("Maximum queue capacity exceeded: " + MAX_CAPACITY);
        }
        final int index = masked(head);
        scheduledTasks[index] = task;
        this.head = head + 1;
    }

    private void flush() {
        if (flushing) {
            return;
        }

        flushing = true;
        try {
            while (tail != head) {
                final int index = masked(tail++);
                final var task = scheduledTasks[index];
                scheduledTasks[index] = null;
                task.run();
            }
            resetPointers();
        } finally {
            flushing = false;
        }
    }

    private void resetPointers() {
        head = 0;
        tail = 0;
    }

    @Override
    public void batch(Runnable batch) {
        assertOwnerThread();
        requireNonNull(batch, "batch");

        batchUnchecked(batch);
    }

    public void batchUnchecked(Runnable batch) {
        enterBatch();
        boolean completed = false;
        try {
            batch.run();
            completed = true;
        } finally {
            exitBatch();
            if (completed && notBatching()) {
                flush();
            }
        }
    }

    private boolean notBatching() {
        return batchLevel == 0;
    }

    private void enterBatch() {
        batchLevel++;
    }

    private void exitBatch() {
        batchLevel--;
    }

    void assertOwnerThread() {
        if (ownerThread != Thread.currentThread()) {
            throw new WrongThreadException("Called scheduler on non-owner thread");
        }
    }

}

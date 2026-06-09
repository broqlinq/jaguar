package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scheduler;

import java.util.ArrayDeque;
import java.util.Objects;

public final class SchedulerImpl implements Scheduler {

    private static final int MAX_CAPACITY = 0x1 << 14;
    private static final int MASK = MAX_CAPACITY - 1;

    private final Thread ownerThread;

    private final Runnable[] scheduledTasks = new Runnable[MAX_CAPACITY];
    private int head = 0;
    private int tail = 0;

    private int processLevel = 0;
    private boolean flushing = false;

    public SchedulerImpl(Thread ownerThread) {
        Objects.requireNonNull(ownerThread, "owner thread cannot be null");
        this.ownerThread = ownerThread;
    }

    private static int masked(int i) {
        return i & MASK;
    }

    @Override
    public void schedule(Runnable task) {
        Objects.requireNonNull(task, "task");
        assertOwnerThread();
        scheduleUnchecked(task);
    }

    public void scheduleUnchecked(Runnable task) {
        enqueue(task);

        if (notProcessing()) {
            flush();
        }
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
    public void process(Runnable batch) {
        Objects.requireNonNull(batch, "batch");
        assertOwnerThread();

        processUnchecked(batch);
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

    private boolean notProcessing() {
        return processLevel == 0;
    }

    public void processUnchecked(Runnable batch) {
        enterProcess();
        boolean completed = false;
        try {
            batch.run();
            completed = true;
        } finally {
            exitProcess();
            if (completed && notProcessing()) {
                flush();
            }
        }
    }

    private void enterProcess() {
        processLevel++;
    }

    private void exitProcess() {
        processLevel--;
    }

    private void assertOwnerThread() {
        if (ownerThread != Thread.currentThread()) {
            throw new WrongThreadException("Called scheduler on non-owner thread");
        }
    }

    ArrayDeque<Runnable> debugAsDeque() {
        final int head = this.head;
        int tail = this.tail;
        ArrayDeque<Runnable> deque = new ArrayDeque<>(head - tail + 1);
        while (tail != head) {
            final int index = masked(tail++);
            final var task = scheduledTasks[index];
            deque.push(task);
        }
        return deque;
    }
}

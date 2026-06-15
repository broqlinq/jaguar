package io.github.broqlinq.jaguar.reactive.internal;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerImplTest {


    @Test
    void scheduleRunsTaskImmediatelyWhenNotBatching() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.schedule(() -> events.add("A"));

        assertEquals(List.of("A"), events);
    }

    @Test
    void scheduleRunsTasksInFifoOrderWhenNotBatching() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.schedule(() -> events.add("A"));
        scheduler.schedule(() -> events.add("B"));
        scheduler.schedule(() -> events.add("C"));

        assertEquals(List.of("A", "B", "C"), events);
    }

    @Test
    void batchRunsBodyImmediately() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.batch(() -> events.add("body"));

        assertEquals(List.of("body"), events);
    }

    @Test
    void scheduleInsideProcessIsDelayedUntilBatchCompletes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.batch(() -> {
            events.add("before");
            scheduler.schedule(() -> events.add("scheduled"));
            events.add("after");

            assertEquals(List.of("before", "after"), events);
        });

        assertEquals(List.of("before", "after", "scheduled"), events);
    }

    @Test
    void scheduleInsideProcessFlushesInFifoOrderAfterBatchCompletes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.batch(() -> {
            scheduler.schedule(() -> events.add("A"));
            scheduler.schedule(() -> events.add("B"));
            scheduler.schedule(() -> events.add("C"));
            events.add("body");
        });

        assertEquals(List.of("body", "A", "B", "C"), events);
    }

    @Test
    void nestedProcessFlushesOnlyAfterOutermostBatchCompletes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.batch(() -> {
            events.add("outer-start");

            scheduler.schedule(() -> events.add("A"));

            scheduler.batch(() -> {
                events.add("inner-start");
                scheduler.schedule(() -> events.add("B"));
                events.add("inner-end");
            });

            events.add("outer-end");

            assertEquals(
                    List.of("outer-start", "inner-start", "inner-end", "outer-end"),
                    events
            );
        });

        assertEquals(
                List.of("outer-start", "inner-start", "inner-end", "outer-end", "A", "B"),
                events
        );
    }

    @Test
    void taskScheduledWhileFlushingRunsInSameFlush() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.batch(() -> {
            scheduler.schedule(() -> {
                events.add("A");
                scheduler.schedule(() -> events.add("B"));
                events.add("C");
            });
        });

        assertEquals(List.of("A", "C", "B"), events);
    }

    @Test
    void deeplyReentrantSchedulingPreservesFifoOrder() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<Integer>();

        scheduler.schedule(() -> {
            events.add(1);

            scheduler.schedule(() -> {
                events.add(3);
                scheduler.schedule(() -> events.add(5));
            });

            scheduler.schedule(() -> events.add(4));

            events.add(2);
        });

        assertEquals(List.of(1, 2, 3, 4, 5), events);
    }

    @Test
    void batchDoesNotFlushIfBodyThrows() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        var thrown = assertThrows(IllegalStateException.class, () -> {
            scheduler.batch(() -> {
                scheduler.schedule(() -> events.add("scheduled"));
                events.add("body");
                throw new IllegalStateException("boom");
            });
        });

        assertEquals("boom", thrown.getMessage());
        assertEquals(List.of("body"), events);
    }

    @Test
    void tasksQueuedByFailedBatchCanRunOnLaterFlush() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        assertThrows(IllegalStateException.class, () -> {
            scheduler.batch(() -> {
                scheduler.schedule(() -> events.add("from-failed-process"));
                throw new IllegalStateException("boom");
            });
        });

        assertEquals(List.of(), events);

        scheduler.schedule(() -> events.add("later"));

        assertEquals(List.of("from-failed-process", "later"), events);
    }

    @Test
    void flushStopsWhenScheduledTaskThrows() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        var thrown = assertThrows(IllegalStateException.class, () -> {
            scheduler.batch(() -> {
                scheduler.schedule(() -> {
                    events.add("A");
                    throw new IllegalStateException("boom");
                });
                scheduler.schedule(() -> events.add("B"));
            });
        });

        assertEquals("boom", thrown.getMessage());
        assertEquals(List.of("A"), events);
    }

    @Test
    void remainingTasksAfterThrownScheduledTaskCanRunOnLaterFlush() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        assertThrows(IllegalStateException.class, () -> {
            scheduler.batch(() -> {
                scheduler.schedule(() -> {
                    events.add("A");
                    throw new IllegalStateException("boom");
                });
                scheduler.schedule(() -> events.add("B"));
            });
        });

        scheduler.schedule(() -> events.add("C"));

        assertEquals(List.of("A", "B", "C"), events);
    }

    @Test
    void scheduleRejectsNullTask() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(NullPointerException.class, () -> scheduler.schedule(null));
    }

    @Test
    void batchRejectsNullBatch() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(NullPointerException.class, () -> scheduler.batch(null));
    }

    @Test
    void scheduleRejectsWrongThread() throws Exception {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var failure = new AtomicReference<Throwable>();

        var thread = new Thread(() -> {
            try {
                scheduler.schedule(() -> {});
            } catch (Throwable ex) {
                failure.set(ex);
            }
        });

        thread.start();
        thread.join();

        assertInstanceOf(WrongThreadException.class, failure.get());
    }

    @Test
    void batchRejectsWrongThread() throws Exception {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var failure = new AtomicReference<Throwable>();

        var thread = new Thread(() -> {
            try {
                scheduler.batch(() -> {});
            } catch (Throwable ex) {
                failure.set(ex);
            }
        });

        thread.start();
        thread.join();

        assertInstanceOf(WrongThreadException.class, failure.get());
    }

    @Test
    void scheduleUncheckedDoesNotRejectWrongThread() throws Exception {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var failure = new AtomicReference<Throwable>();
        var ran = new AtomicReference<Boolean>(false);

        var thread = new Thread(() -> {
            try {
                scheduler.scheduleUnchecked(() -> ran.set(true));
            } catch (Throwable ex) {
                failure.set(ex);
            }
        });

        thread.start();
        thread.join();

        assertNull(failure.get());
        assertTrue(ran.get());
    }

    @Test
    void batchUncheckedDoesNotRejectWrongThread() throws Exception {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var failure = new AtomicReference<Throwable>();
        var ran = new AtomicReference<Boolean>(false);

        var thread = new Thread(() -> {
            try {
                scheduler.batchUnchecked(() -> ran.set(true));
            } catch (Throwable ex) {
                failure.set(ex);
            }
        });

        thread.start();
        thread.join();

        assertNull(failure.get());
        assertTrue(ran.get());
    }

    @Test
    void supportsCapacityMinusOneQueuedTasksInsideBatch() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var counter = new int[1];

        scheduler.batch(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> counter[0]++);
            }

            assertEquals(0, counter[0]);
        });

        assertEquals(16_384, counter[0]);
    }

    @Test
    void throwsWhenMaximumQueueCapacityExceededInsideBatch() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(IllegalStateException.class, () -> {
            scheduler.batch(() -> {
                for (int i = 0; i < 16_384; i++) {
                    scheduler.schedule(() -> {});
                }

                scheduler.schedule(() -> {});
            });
        });
    }

    @Test
    void schedulerCanBeUsedAfterFullQueueIsFlushed() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var counter = new int[1];

        scheduler.batch(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> counter[0]++);
            }
        });

        scheduler.batch(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> counter[0]++);
            }
        });

        assertEquals(32_768, counter[0]);
    }

    @Test
    void batchCanBeCalledFromScheduledTask() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.schedule(() -> {
            events.add("A");

            scheduler.batch(() -> {
                events.add("B");
                scheduler.schedule(() -> events.add("D"));
                events.add("C");
            });

            events.add("E");
        });

        assertEquals(List.of("A", "B", "C", "E", "D"), events);
    }

    @Test
    void taskScheduledFromNestedBatchDuringFlushRunsAfterCurrentFlushWork() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.batch(() -> {
            scheduler.schedule(() -> {
                events.add("A");

                scheduler.batch(() -> {
                    events.add("B");
                    scheduler.schedule(() -> events.add("D"));
                    events.add("C");
                });

                events.add("E");
            });

            scheduler.schedule(() -> events.add("F"));
        });

        assertEquals(List.of("A", "B", "C", "E", "F", "D"), events);
    }

    @Test
    void uncheckedScheduleRejectsNullEventuallyWithNullPointerException() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(NullPointerException.class, () -> scheduler.scheduleUnchecked(null));
    }

    @Test
    void uncheckedBatchRejectsNullEventuallyWithNullPointerException() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(NullPointerException.class, () -> scheduler.batchUnchecked(null));
    }

    @Test
    void queueContinuesToWorkAfterManySmallFlushes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var counter = new int[1];

        for (int i = 0; i < 100_000; i++) {
            scheduler.schedule(() -> counter[0]++);
        }

        assertEquals(100_000, counter[0]);
    }

    @Test
    void queueContinuesToWorkAfterManyBatchFlushes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var counter = new int[1];

        for (int i = 0; i < 10_000; i++) {
            scheduler.batch(() -> scheduler.schedule(() -> counter[0]++));
        }

        assertEquals(10_000, counter[0]);
    }

    @Test
    void nullOwnerThreadIsRejected() {
        assertThrows(NullPointerException.class, () -> new SchedulerImpl(null));
    }
}

package io.github.broqlinq.jaguar.reactive.internal;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerImplTest {


    @Test
    void scheduleRunsTaskImmediatelyWhenNotProcessing() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.schedule(() -> events.add("A"));

        assertEquals(List.of("A"), events);
    }

    @Test
    void scheduleRunsTasksInFifoOrderWhenNotProcessing() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.schedule(() -> events.add("A"));
        scheduler.schedule(() -> events.add("B"));
        scheduler.schedule(() -> events.add("C"));

        assertEquals(List.of("A", "B", "C"), events);
    }

    @Test
    void processRunsBodyImmediately() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.process(() -> events.add("body"));

        assertEquals(List.of("body"), events);
    }

    @Test
    void scheduleInsideProcessIsDelayedUntilProcessCompletes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.process(() -> {
            events.add("before");
            scheduler.schedule(() -> events.add("scheduled"));
            events.add("after");

            assertEquals(List.of("before", "after"), events);
        });

        assertEquals(List.of("before", "after", "scheduled"), events);
    }

    @Test
    void scheduleInsideProcessFlushesInFifoOrderAfterProcessCompletes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.process(() -> {
            scheduler.schedule(() -> events.add("A"));
            scheduler.schedule(() -> events.add("B"));
            scheduler.schedule(() -> events.add("C"));
            events.add("body");
        });

        assertEquals(List.of("body", "A", "B", "C"), events);
    }

    @Test
    void nestedProcessFlushesOnlyAfterOutermostProcessCompletes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.process(() -> {
            events.add("outer-start");

            scheduler.schedule(() -> events.add("A"));

            scheduler.process(() -> {
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

        scheduler.process(() -> {
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
    void processDoesNotFlushIfBodyThrows() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        var thrown = assertThrows(IllegalStateException.class, () -> {
            scheduler.process(() -> {
                scheduler.schedule(() -> events.add("scheduled"));
                events.add("body");
                throw new IllegalStateException("boom");
            });
        });

        assertEquals("boom", thrown.getMessage());
        assertEquals(List.of("body"), events);
    }

    @Test
    void tasksQueuedByFailedProcessCanRunOnLaterFlush() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        assertThrows(IllegalStateException.class, () -> {
            scheduler.process(() -> {
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
            scheduler.process(() -> {
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
            scheduler.process(() -> {
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
    void processRejectsNullBatch() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(NullPointerException.class, () -> scheduler.process(null));
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
    void processRejectsWrongThread() throws Exception {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var failure = new AtomicReference<Throwable>();

        var thread = new Thread(() -> {
            try {
                scheduler.process(() -> {});
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
    void processUncheckedDoesNotRejectWrongThread() throws Exception {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var failure = new AtomicReference<Throwable>();
        var ran = new AtomicReference<Boolean>(false);

        var thread = new Thread(() -> {
            try {
                scheduler.processUnchecked(() -> ran.set(true));
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
    void supportsCapacityMinusOneQueuedTasksInsideProcess() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var counter = new int[1];

        scheduler.process(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> counter[0]++);
            }

            assertEquals(0, counter[0]);
        });

        assertEquals(16_384, counter[0]);
    }

    @Test
    void throwsWhenMaximumQueueCapacityExceededInsideProcess() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(IllegalStateException.class, () -> {
            scheduler.process(() -> {
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

        scheduler.process(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> counter[0]++);
            }
        });

        scheduler.process(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> counter[0]++);
            }
        });

        assertEquals(32_768, counter[0]);
    }

    @Test
    void processCanBeCalledFromScheduledTask() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.schedule(() -> {
            events.add("A");

            scheduler.process(() -> {
                events.add("B");
                scheduler.schedule(() -> events.add("D"));
                events.add("C");
            });

            events.add("E");
        });

        assertEquals(List.of("A", "B", "C", "E", "D"), events);
    }

    @Test
    void taskScheduledFromNestedProcessDuringFlushRunsAfterCurrentFlushWork() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var events = new ArrayList<String>();

        scheduler.process(() -> {
            scheduler.schedule(() -> {
                events.add("A");

                scheduler.process(() -> {
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
    void uncheckedProcessRejectsNullEventuallyWithNullPointerException() {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        assertThrows(NullPointerException.class, () -> scheduler.processUnchecked(null));
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
    void queueContinuesToWorkAfterManyProcessFlushes() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var counter = new int[1];

        for (int i = 0; i < 10_000; i++) {
            scheduler.process(() -> scheduler.schedule(() -> counter[0]++));
        }

        assertEquals(10_000, counter[0]);
    }

    @Test
    void nullOwnerThreadIsRejected() {
        assertThrows(NullPointerException.class, () -> new SchedulerImpl(null));
    }
}

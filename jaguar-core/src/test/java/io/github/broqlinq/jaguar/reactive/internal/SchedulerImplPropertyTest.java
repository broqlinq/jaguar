package io.github.broqlinq.jaguar.reactive.internal;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerImplPropertyTest {

    sealed interface Op {
        record Schedule(int id) implements Op {}
        record Process(List<Op> ops) implements Op {}
    }

    @Property
    void scheduledTasksRunExactlyOnceInFifoOrder(
            @ForAll("flatSchedules") List<Integer> ids
    ) {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var actual = new ArrayList<Integer>();

        scheduler.process(() -> {
            for (int id : ids) {
                scheduler.schedule(() -> actual.add(id));
            }

            assertTrue(actual.isEmpty());
        });

        assertEquals(ids, actual);
    }

    @Provide
    Arbitrary<List<Integer>> flatSchedules() {
        return Arbitraries.integers()
                .between(0, 10_000)
                .list()
                .ofMinSize(0)
                .ofMaxSize(1_000);
    }

    @Property
    void processDelaysAllScheduledTasksUntilEnd(
            @ForAll("taskIds") List<Integer> ids
    ) {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var actual = new ArrayList<Integer>();

        scheduler.process(() -> {
            for (int id : ids) {
                scheduler.schedule(() -> actual.add(id));
            }

            assertEquals(List.of(), actual);
        });

        assertEquals(ids, actual);
    }

    @Property
    void nestedProcessesStillFlushOnlyAfterOutermostProcess(
            @ForAll("taskIds") List<Integer> outer,
            @ForAll("taskIds") List<Integer> inner
    ) {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var actual = new ArrayList<Integer>();
        var expected = new ArrayList<Integer>();

        scheduler.process(() -> {
            for (int id : outer) {
                scheduler.schedule(() -> actual.add(id));
                expected.add(id);
            }

            scheduler.process(() -> {
                for (int id : inner) {
                    scheduler.schedule(() -> actual.add(id));
                    expected.add(id);
                }

                assertTrue(actual.isEmpty());
            });

            assertTrue(actual.isEmpty());
        });

        assertEquals(expected, actual);
    }

    @Provide
    Arbitrary<List<Integer>> taskIds() {
        return Arbitraries.integers()
                .between(0, 10_000)
                .list()
                .ofMinSize(0)
                .ofMaxSize(1_000);
    }

    @Property
    void flatScheduledTasksRunInFifoOrder(
            @ForAll("taskIds") List<Integer> ids
    ) {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var actual = new ArrayList<Integer>();

        scheduler.process(() -> {
            for (int id : ids) {
                scheduler.schedule(() -> actual.add(id));
            }

            assertTrue(actual.isEmpty());
        });

        assertEquals(ids, actual);
    }

    @Property
    void nestedProcessPreservesScheduleOrder(
            @ForAll("taskIds") List<Integer> outer,
            @ForAll("taskIds") List<Integer> inner
    ) {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        var actual = new ArrayList<Integer>();
        var expected = new ArrayList<Integer>();

        scheduler.process(() -> {
            for (int id : outer) {
                expected.add(id);
                scheduler.schedule(() -> actual.add(id));
            }

            scheduler.process(() -> {
                for (int id : inner) {
                    expected.add(id);
                    scheduler.schedule(() -> actual.add(id));
                }

                assertTrue(actual.isEmpty());
            });

            assertTrue(actual.isEmpty());
        });

        assertEquals(expected, actual);
    }

    @Property
    void scheduledTasksMayScheduleMoreTasksAndStillRunFifo(
            @ForAll("taskIds") List<Integer> first,
            @ForAll("taskIds") List<Integer> second
    ) {
        var scheduler = new SchedulerImpl(Thread.currentThread());

        var actual = new ArrayList<Integer>();
        var expected = new ArrayList<Integer>();

        expected.addAll(first);
        expected.addAll(second);

        scheduler.process(() -> {
            for (int id : first) {
                scheduler.schedule(() -> actual.add(id));
            }

            scheduler.schedule(() -> {
                for (int id : second) {
                    scheduler.schedule(() -> actual.add(id));
                }
            });
        });

        assertEquals(expected, actual);
    }

    @Test
    void failedProcessDoesNotFlushButQueuedTasksSurvive() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var actual = new ArrayList<String>();

        assertThrows(IllegalStateException.class, () -> {
            scheduler.process(() -> {
                scheduler.schedule(() -> actual.add("A"));
                throw new IllegalStateException("boom");
            });
        });

        assertEquals(List.of(), actual);

        scheduler.schedule(() -> actual.add("B"));

        assertEquals(List.of("A", "B"), actual);
    }

    @Test
    void throwsWhenCapacityExceededInsideProcess() {
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
    void scheduledTaskThrowingStopsFlushButRemainingTasksSurvive() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var actual = new ArrayList<String>();

        assertThrows(IllegalStateException.class, () -> {
            scheduler.process(() -> {
                scheduler.schedule(() -> {
                    actual.add("A");
                    throw new IllegalStateException("boom");
                });
                scheduler.schedule(() -> actual.add("B"));
            });
        });

        assertEquals(List.of("A"), actual);

        scheduler.schedule(() -> actual.add("C"));

        assertEquals(List.of("A", "B", "C"), actual);
    }

    @Test
    void schedulerCanBeReusedAfterFullCapacityFlush() {
        var scheduler = new SchedulerImpl(Thread.currentThread());
        var count = new int[1];

        scheduler.process(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> count[0]++);
            }
        });

        scheduler.process(() -> {
            for (int i = 0; i < 16_384; i++) {
                scheduler.schedule(() -> count[0]++);
            }
        });

        assertEquals(32_768, count[0]);
    }
}

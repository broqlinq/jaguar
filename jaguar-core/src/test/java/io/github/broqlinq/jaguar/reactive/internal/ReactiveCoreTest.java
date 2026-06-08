package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Effect;
import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.reactive.State;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


final class ReactiveCoreTest {

    private static ThreadConfinedScope scope() {
        return new ThreadConfinedScope("test", new ThreadConfinedScheduler(Thread.currentThread()));
    }

    private static Throwable runOnOtherThread(ThrowingRunnable runnable) throws InterruptedException {
        AtomicReference<Throwable> thrown = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                runnable.run();
            } catch (Throwable error) {
                thrown.set(error);
            }
        });

        thread.start();
        thread.join();

        return thrown.get();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @Nested
    final class SchedulerTests {

        @Test
        void scheduleRunsTaskImmediatelyWhenNotBatching() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());
            var events = new ArrayList<String>();

            scheduler.schedule(() -> events.add("A"));
            scheduler.schedule(() -> events.add("B"));

            assertEquals(List.of("A", "B"), events);
        }

        @Test
        void scheduleUsesFifoOrder() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());
            var events = new ArrayList<String>();

            scheduler.schedule(() -> events.add("A"));
            scheduler.schedule(() -> events.add("B"));
            scheduler.schedule(() -> events.add("C"));

            assertEquals(List.of("A", "B", "C"), events);
        }

        @Test
        void scheduleIsReentrancySafe() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());
            var events = new ArrayList<String>();

            scheduler.schedule(() -> {
                events.add("A");
                scheduler.schedule(() -> events.add("B"));
                events.add("C");
            });

            assertEquals(List.of("A", "C", "B"), events);
        }

        @Test
        void batchDefersScheduledTasksUntilBatchExits() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());
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
        void nestedBatchFlushesOnlyAfterOutermostBatchExits() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());
            var events = new ArrayList<String>();

            scheduler.batch(() -> {
                events.add("outer-before");

                scheduler.schedule(() -> events.add("A"));

                scheduler.batch(() -> {
                    events.add("inner");
                    scheduler.schedule(() -> events.add("B"));
                });

                events.add("outer-after");

                assertEquals(List.of("outer-before", "inner", "outer-after"), events);
            });

            assertEquals(List.of("outer-before", "inner", "outer-after", "A", "B"), events);
        }

        @Test
        void failedBatchDoesNotFlushQueuedTasks() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());
            var events = new ArrayList<String>();

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
                scheduler.batch(() -> {
                    scheduler.schedule(() -> events.add("scheduled"));
                    throw new RuntimeException("boom");
                });
            });

            assertEquals("boom", thrown.getMessage());
            assertEquals(List.of(), events);

            scheduler.schedule(() -> events.add("later"));

            assertEquals(List.of("scheduled", "later"), events);
        }

        @Test
        void scheduleFailsFromNonOwnerThread() throws Exception {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());

            Throwable thrown = runOnOtherThread(() -> scheduler.schedule(() -> {
            }));

            assertNotNull(thrown);
            assertInstanceOf(WrongThreadException.class, thrown);
        }

        @Test
        void batchFailsFromNonOwnerThread() throws Exception {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());

            Throwable thrown = runOnOtherThread(() -> scheduler.batch(() -> {
            }));

            assertNotNull(thrown);
            assertInstanceOf(WrongThreadException.class, thrown);
        }
    }

    @Nested
    final class StateTests {

        @Test
        void stateReturnsInitialValue() {
            var scope = scope();

            State<Integer> count = scope.state(10, Objects::equals);

            assertEquals(10, count.value());
        }

        @Test
        void setChangesValue() {
            var scope = scope();
            State<Integer> count = scope.state(0, Objects::equals);

            count.set(5);

            assertEquals(5, count.value());
        }

        @Test
        void setDoesNotInvalidateWhenEqualitySaysEqual() {
            var scope = scope();
            State<Integer> count = scope.state(0, Objects::equals);

            var runs = new ArrayList<Integer>();

            scope.effect(() -> runs.add(count.value()));

            assertEquals(List.of(0), runs);

            count.set(0);

            assertEquals(List.of(0), runs);
        }

        @Test
        void updateChangesValue() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            count.update(value -> value + 1);

            assertEquals(2, count.value());
        }

        @Test
        void updateDoesNotInvalidateWhenEqualitySaysEqual() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            var runs = new ArrayList<Integer>();

            scope.effect(() -> runs.add(count.value()));

            count.update(value -> value);

            assertEquals(List.of(1), runs);
        }


        @Test
        void stateReadTracksEffectDependency() {
            var scope = scope();
            State<Integer> count = scope.state(0, Objects::equals);

            var runs = new ArrayList<Integer>();

            scope.effect(() -> runs.add(count.value()));

            count.set(1);
            count.set(2);

            assertEquals(List.of(0, 1, 2), runs);
        }

        @Test
        void stateReadTracksComputedDependency() {
            var scope = scope();
            State<Integer> count = scope.state(2, Objects::equals);

            Signal<Integer> doubled = scope.computed(() -> count.value() * 2);

            assertEquals(4, doubled.value());

            count.set(3);

            assertEquals(6, doubled.value());
        }

        @Test
        void stateAccessAfterDisposeFails() {
            var scope = scope();
            var state = (Scoped) scope.state(1, Objects::equals);

            state.dispose();

            @SuppressWarnings("unchecked") State<Integer> typedState = (State<Integer>) state;

            assertThrows(IllegalStateException.class, typedState::value);
            assertThrows(IllegalStateException.class, () -> typedState.set(2));
        }
    }

    @Nested
    final class ComputedTests {

        @Test
        void computedIsLazy() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            int[] evaluations = {0};

            Signal<Integer> doubled = scope.computed(() -> {
                evaluations[0]++;
                return count.value() * 2;
            });

            assertEquals(0, evaluations[0]);

            assertEquals(2, doubled.value());
            assertEquals(1, evaluations[0]);

            assertEquals(2, doubled.value());
            assertEquals(1, evaluations[0]);
        }

        @Test
        void computedRecomputesAfterDependencyChanges() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            int[] evaluations = {0};

            Signal<Integer> doubled = scope.computed(() -> {
                evaluations[0]++;
                return count.value() * 2;
            });

            assertEquals(2, doubled.value());
            assertEquals(1, evaluations[0]);

            count.set(2);

            assertEquals(1, evaluations[0], "lazy computed should not recompute until read");
            assertEquals(4, doubled.value());
            assertEquals(2, evaluations[0]);
        }

        @Test
        void computedCanDependOnComputed() {
            var scope = scope();
            State<Integer> count = scope.state(2, Objects::equals);

            Signal<Integer> doubled = scope.computed(() -> count.value() * 2);
            Signal<String> label = scope.computed(() -> "Value: " + doubled.value());

            assertEquals("Value: 4", label.value());

            count.set(3);

            assertEquals("Value: 6", label.value());
        }

        @Test
        void computedDeriveTracksSourceComputed() {
            var scope = scope();
            State<Integer> count = scope.state(2, Objects::equals);

            Signal<Integer> doubled = scope.computed(() -> count.value() * 2);
            Signal<String> label = doubled.derive(value -> "Value: " + value);

            assertEquals("Value: 4", label.value());

            count.set(3);

            assertEquals("Value: 6", label.value());
        }

        @Test
        void computedTransformReturnsImmediateMappedValue() {
            var scope = scope();
            State<Integer> count = scope.state(3, Objects::equals);
            Signal<Integer> doubled = scope.computed(() -> count.value() * 2);

            String result = doubled.transform(value -> "Value: " + value);

            assertEquals("Value: 6", result);
        }

        @Test
        void dynamicDependenciesAreCleanedUp() {
            var scope = scope();

            State<Boolean> useA = scope.state(true, Objects::equals);
            State<Integer> a = scope.state(1, Objects::equals);
            State<Integer> b = scope.state(10, Objects::equals);

            int[] evaluations = {0};

            Signal<Integer> selected = scope.computed(() -> {
                evaluations[0]++;
                return useA.value() ? a.value() : b.value();
            });

            assertEquals(1, selected.value());
            assertEquals(1, evaluations[0]);

            useA.set(false);

            assertEquals(10, selected.value());
            assertEquals(2, evaluations[0]);

            a.set(2);

            assertEquals(2, evaluations[0], "old branch must not invalidate computed");

            b.set(11);

            assertEquals(11, selected.value());
            assertEquals(3, evaluations[0]);
        }

        @Test
        void duplicateDependencyReadDoesNotCauseDuplicateEffectRerun() {
            var scope = scope();

            State<Integer> count = scope.state(1, Objects::equals);
            Signal<Integer> doubledRead = scope.computed(() -> count.value() + count.value());

            var observed = new ArrayList<Integer>();

            scope.effect(() -> observed.add(doubledRead.value()));

            assertEquals(List.of(2), observed);

            count.set(2);

            assertEquals(List.of(2, 4), observed);
        }

        @Test
        void circularComputedDependencyFails() {
            var scope = scope();

            @SuppressWarnings("unchecked") Signal<Integer>[] self = new Signal[1];

            self[0] = scope.computed(() -> self[0].value() + 1);

            assertThrows(IllegalStateException.class, () -> self[0].value());
        }

        @Test
        void computedAccessAfterScopeCloseFails() {
            var scope = scope();

            Signal<Integer> value = scope.computed(() -> 1);

            assertEquals(1, value.value());

            scope.close();

            assertThrows(IllegalStateException.class, value::value);
        }
    }

    @Nested
    final class EffectTests {

        @Test
        void effectRunsInitially() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            var values = new ArrayList<Integer>();

            scope.effect(() -> values.add(count.value()));

            assertEquals(List.of(1), values);
        }

        @Test
        void effectRerunsWhenDependencyChanges() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            var values = new ArrayList<Integer>();

            scope.effect(() -> values.add(count.value()));

            count.set(2);
            count.set(3);

            assertEquals(List.of(1, 2, 3), values);
        }

        @Test
        void effectDoesNotRerunWhenUnrelatedStateChanges() {
            var scope = scope();

            State<Integer> used = scope.state(1, Objects::equals);
            State<Integer> unused = scope.state(100, Objects::equals);

            var values = new ArrayList<Integer>();

            scope.effect(() -> values.add(used.value()));

            unused.set(101);

            assertEquals(List.of(1), values);
        }

        @Test
        void effectDynamicDependenciesAreCleanedUp() {
            var scope = scope();

            State<Boolean> useA = scope.state(true, Objects::equals);
            State<Integer> a = scope.state(1, Objects::equals);
            State<Integer> b = scope.state(10, Objects::equals);

            var values = new ArrayList<Integer>();

            scope.effect(() -> values.add(useA.value() ? a.value() : b.value()));

            assertEquals(List.of(1), values);

            useA.set(false);

            assertEquals(List.of(1, 10), values);

            a.set(2);

            assertEquals(List.of(1, 10), values, "old branch must not rerun effect");

            b.set(11);

            assertEquals(List.of(1, 10, 11), values);
        }

        @Test
        void effectDisposeStopsFutureReruns() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            var values = new ArrayList<Integer>();

            Effect effect = scope.effect(() -> values.add(count.value()));

            assertEquals(List.of(1), values);

            effect.dispose();

            count.set(2);

            assertEquals(List.of(1), values);
        }

        @Test
        void disposingEffectTwiceIsSafe() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            Effect effect = scope.effect(count::value);

            effect.dispose();
            effect.dispose();

            count.set(2);

            assertDoesNotThrow(() -> count.set(3));
        }

        @Test
        void effectCanDependOnComputed() {
            var scope = scope();

            State<Integer> count = scope.state(1, Objects::equals);
            Signal<Integer> doubled = scope.computed(() -> count.value() * 2);

            var values = new ArrayList<Integer>();

            scope.effect(() -> values.add(doubled.value()));

            count.set(2);

            assertEquals(List.of(2, 4), values);
        }

        @Test
        void effectFailureEscapes() {
            var scope = scope();
            State<Integer> count = scope.state(1, Objects::equals);

            RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
                scope.effect(() -> {
                    count.value();
                    throw new RuntimeException("boom");
                });
            });

            assertEquals("boom", thrown.getMessage());
        }
    }

    @Nested
    final class ScopeTests {

        @Test
        void blankScopeNameIsNormalized() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());

            var scope = new ThreadConfinedScope("  ", scheduler);

            assertEquals("<unnamed-scope>", scope.name());
        }

        @Test
        void childScopeInheritsScheduler() {
            var scheduler = new ThreadConfinedScheduler(Thread.currentThread());
            var parent = new ThreadConfinedScope("parent", scheduler);

            var child = parent.child("child");

            assertSame(scheduler, child.scheduler());
        }

        @Test
        void parentCloseClosesChildScope() {
            var parent = scope();
            var child = parent.child("child");

            State<Integer> childState = child.state(1, Objects::equals);

            parent.close();

            assertTrue(parent.isClosed());
            assertTrue(child.isClosed());

            assertThrows(IllegalStateException.class, childState::value);
        }

        @Test
        void closingScopeDisposesState() {
            var scope = scope();

            State<Integer> state = scope.state(1, Objects::equals);

            scope.close();

            assertThrows(IllegalStateException.class, state::value);
            assertThrows(IllegalStateException.class, () -> state.set(2));
        }

        @Test
        void closingScopeDisposesComputed() {
            var scope = scope();

            Signal<Integer> computed = scope.computed(() -> 1);

            assertEquals(1, computed.value());

            scope.close();

            assertThrows(IllegalStateException.class, computed::value);
        }

        @Test
        void closingScopeDisposesEffect() {
            var scope = scope();

            State<Integer> count = scope.state(1, Objects::equals);
            var values = new ArrayList<Integer>();

            scope.effect(() -> values.add(count.value()));

            assertEquals(List.of(1), values);

            scope.close();

            assertThrows(IllegalStateException.class, () -> count.set(2));
            assertEquals(List.of(1), values);
        }

        @Test
        void closeIsIdempotent() {
            var scope = scope();

            scope.state(1, Objects::equals);
            scope.computed(() -> 2);
            scope.effect(() -> {
            });

            assertDoesNotThrow(scope::close);
            assertDoesNotThrow(scope::close);
        }

        @Test
        void creatingStateAfterCloseFails() {
            var scope = scope();

            scope.close();

            assertThrows(IllegalStateException.class, () -> scope.state(1, Objects::equals));
        }

        @Test
        void creatingComputedAfterCloseFails() {
            var scope = scope();

            scope.close();

            assertThrows(IllegalStateException.class, () -> scope.computed(() -> 1));
        }

        @Test
        void creatingEffectAfterCloseFails() {
            var scope = scope();

            scope.close();

            assertThrows(IllegalStateException.class, () -> scope.effect(() -> {
            }));
        }

        @Test
        void stateAccessFromNonOwnerThreadFails() throws Exception {
            var scope = scope();
            State<Integer> state = scope.state(1, Objects::equals);

            Throwable readFailure = runOnOtherThread(state::value);
            Throwable writeFailure = runOnOtherThread(() -> state.set(2));

            assertNotNull(readFailure);
            assertInstanceOf(WrongThreadException.class, readFailure);

            assertNotNull(writeFailure);
            assertInstanceOf(WrongThreadException.class, writeFailure);
        }

        @Test
        void scopeAccessFromNonOwnerThreadFails() throws Exception {
            var scope = scope();

            Throwable failure = runOnOtherThread(() -> scope.state(1, Objects::equals));

            assertNotNull(failure);
            assertInstanceOf(WrongThreadException.class, failure);
        }

        @Test
        void scopeCloseFromNonOwnerThreadFails() throws Exception {
            var scope = scope();

            Throwable failure = runOnOtherThread(scope::close);

            assertNotNull(failure);
            assertInstanceOf(WrongThreadException.class, failure);

            assertFalse(scope.isClosed());
        }
    }

    @Nested
    final class BatchIntegrationTests {

        @Test
        void batchCoalescesEffectReruns() {
            try (var scope = scope()) {
                ArrayList<Integer> sums;

                State<Integer> a = scope.state(1, Objects::equals);
                State<Integer> b = scope.state(10, Objects::equals);

                sums = new ArrayList<>();

                scope.effect(() -> sums.add(a.value() + b.value()));

                assertEquals(List.of(11), sums);

                scope.batch(() -> {
                    a.set(2);
                    b.set(20);

                    assertEquals(List.of(11), sums);
                });
                assertEquals(List.of(11, 22), sums);
            }

        }

        @Test
        void nestedBatchCoalescesEffectRerunsUntilOuterBatchExits() {
            ArrayList<Integer> sums;
            try (var scope = scope()) {

                State<Integer> a = scope.state(1, Objects::equals);
                State<Integer> b = scope.state(10, Objects::equals);
                State<Integer> c = scope.state(100, Objects::equals);

                sums = new ArrayList<Integer>();

                scope.effect(() -> sums.add(a.value() + b.value() + c.value()));

                assertEquals(List.of(111), sums);

                scope.batch(() -> {
                    a.set(2);

                    scope.batch(() -> {
                        b.set(20);
                        c.set(200);
                    });

                    assertEquals(List.of(111), sums);
                });
            }

            assertEquals(List.of(111, 222), sums);
        }

        @Test
        void failedBatchDoesNotRunEffectsImmediately() {
            var scope = scope();

            State<Integer> count = scope.state(0, Objects::equals);
            var values = new ArrayList<Integer>();

            scope.effect(() -> values.add(count.value()));

            assertEquals(List.of(0), values);

            assertThrows(RuntimeException.class, () -> {
                scope.batch(() -> {
                    count.set(1);
                    throw new RuntimeException("boom");
                });
            });

            assertEquals(List.of(0), values);

            scope.schedule(() -> {
            });

            assertEquals(List.of(0, 1), values);
        }
    }
}

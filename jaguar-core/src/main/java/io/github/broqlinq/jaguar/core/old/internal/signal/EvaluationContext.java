package io.github.broqlinq.jaguar.core.old.internal.signal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class EvaluationContext {

    private static final ThreadLocal<ObserverStack> STACK = ThreadLocal.withInitial(ObserverStack::new);

    public static void push(Observer observer) {
        var stack = STACK.get();
        stack.push(observer);
    }

    public static Observer pop() {
        var stack = STACK.get();
        return stack.pop();
    }

    public static Observer peek() {
        var stack = STACK.get();
        return stack.peek();
    }

    private record ObserverStack(Deque<Observer> stack, Set<Observer> visited) {

        ObserverStack() {
            this(new ArrayDeque<>(), new HashSet<>());
        }

        void push(Observer observer) {
            if (visited.add(observer)) {
                stack.push(observer);
            } else {
                throw new IllegalStateException("Circular observer dependency detected: " + observer);
            }
        }

        Observer pop() {
            if (stack.isEmpty()) {
                return null;
            }
            var observer = stack.pop();
            visited.remove(observer);
            return observer;
        }

        Observer peek() {
            if (stack.isEmpty()) {
                return null;
            }
            return stack.peek();
        }
    }
}

package io.github.broqlinq.jaguar.core.internal.signal;

import java.util.ArrayDeque;
import java.util.Deque;

public final class EvaluationContext {

    private static final ThreadLocal<Deque<Observer>> CURRENT = ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(Observer observer) {
        var queue = CURRENT.get();
        queue.push(observer);
    }

    public static Observer pop() {
        var queue = CURRENT.get();
        return queue.isEmpty() ? null : queue.pop();
    }

    public static Observer peek() {
        var queue = CURRENT.get();
        return queue.peek();
    }

}

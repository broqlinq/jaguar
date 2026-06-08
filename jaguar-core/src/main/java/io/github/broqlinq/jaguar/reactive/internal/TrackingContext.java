package io.github.broqlinq.jaguar.reactive.internal;

import java.util.Arrays;

final class TrackingContext {

    private Consumer[] stack = new Consumer[128];
    private int size = 0;

    private Consumer peek() {
        if (size == 0) {
            return null;
        }
        return stack[size - 1];
    }

    public void track(Producer producer) {
        if (size > 0) {
            var consumer = stack[size - 1];
            var link = consumer.producerLinks().getOrCreate(producer);
            if (link.active) {
                link.markSeen();
            } else {
                link.link(producer, consumer);
            }
        }
    }

    public void push(Consumer consumer) {
        if (consumer.isTracking()) {
            throw new IllegalStateException("Consumer is already in tracking context, circular dependency detected");
        }
        if (size == stack.length) {
            int newSize = stack.length + stack.length / 2;
            stack = Arrays.copyOf(stack, newSize);
        }
        stack[size++] = consumer;
        consumer.setTracking(true);
    }

    public void pop(Consumer expected) {
        if (size == 0 || stack[size - 1] != expected) {
            throw new IllegalStateException("Tracking stack corrupted");
        }

        Consumer consumer = stack[--size];
        stack[size] = null;
        consumer.setTracking(false);
    }
}

package io.github.broqlinq.jaguar.core.internal.signal;

final class EvaluationSession implements AutoCloseable {

    EvaluationSession(Observer observer) {
        EvaluationContext.push(observer);
    }

    @Override
    public void close() {
        EvaluationContext.pop();
    }
}

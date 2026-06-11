package io.github.broqlinq.jaguar.reactive.internal;

final class ExceptionAccumulator {

    private Throwable ex;

    ExceptionAccumulator runTask(Runnable task) {
        try {
            task.run();
        } catch (Throwable e) {
            accumulate(e);
        }
        return this;
    }

    private void accumulate(Throwable e) {
        if (ex == null) {
            ex = e;
        } else {
            ex.addSuppressed(e);
        }
    }

    void throwIfFailed() {
        switch (ex) {
            case null -> {
            }
            case RuntimeException e -> throw e;
            case Error err -> throw err;
            default -> throw new IllegalStateException("Unexpected checked exception: " + ex);
        }
    }
}

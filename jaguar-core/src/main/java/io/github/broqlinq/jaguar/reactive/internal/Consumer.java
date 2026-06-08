package io.github.broqlinq.jaguar.reactive.internal;

interface Consumer {

    void markInvalid();

    void markStale();

    long epoch();

    ProducerList producerLinks();

    boolean isTracking();

    void setTracking(boolean tracking);

    enum State {
        CLEAN,
        STALE,
        INVALID
    }

    enum Type {
        COMPUTED,
        EFFECT
    }
}

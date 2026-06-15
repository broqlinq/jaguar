package io.github.broqlinq.jaguar.reactive.internal;

public sealed interface TrackingContext permits ConfinedScope {

    ReactiveNode current();

    ReactiveNode push(ReactiveNode node);

    void pop(ReactiveNode previous);

    default void track(ReactiveNode producer) {
        var current = current();
        if (current != null) {
            current.trackDependency(producer);
        }
    }
}

package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.internal.ReactiveNode;

abstract class StateSignalBase extends ReactiveNode {

    StateSignalBase(ConfinedScope scope) {
        super(scope);
    }

    final void commitStateChange() {
        version++;
        notifyDependentsInvalidated();
    }

    final void preRead() {
        scope.assertOpen();
        assertNotDisposed();

        scope.track(this);
    }

    final void preWrite() {
        scope.assertOpen();
        assertNotDisposed();
    }

    @Override
    public final byte dependentKind() {
        throw new IllegalStateException("State signal is not dependent");
    }

    @Override
    protected void onDispose() {
        unlinkAllDependents();
    }
}

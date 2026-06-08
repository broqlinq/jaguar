package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.Scope;

public interface Scoped extends Disposable {

    Scope scope();
}

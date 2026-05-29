package io.github.broqlinq.jaguar.core.signal;

import io.github.broqlinq.jaguar.core.internal.signal.EffectImpl;

public interface Effect extends Runnable{

    @Override
    void run();

    static Effect of(Runnable effect) {
        var e = new EffectImpl(effect);
        e.run();
        return e;
    }
}

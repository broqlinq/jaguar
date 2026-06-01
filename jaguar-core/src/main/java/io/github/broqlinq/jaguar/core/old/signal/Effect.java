package io.github.broqlinq.jaguar.core.old.signal;

import io.github.broqlinq.jaguar.core.old.internal.signal.BatchManager;
import io.github.broqlinq.jaguar.core.old.internal.signal.EffectImpl;

public interface Effect extends Runnable {

    static Effect of(Runnable effect) {
        var e = new EffectImpl(effect);
        BatchManager.schedule(e);
        return e;
    }

    static void schedule(Runnable effect) {
        BatchManager.run(effect);
    }

    @Override
    void run();
}

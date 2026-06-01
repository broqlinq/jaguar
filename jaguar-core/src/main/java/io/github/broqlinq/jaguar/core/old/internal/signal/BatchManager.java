package io.github.broqlinq.jaguar.core.old.internal.signal;

import io.github.broqlinq.jaguar.core.old.signal.Effect;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class BatchManager {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private static final ThreadLocal<Set<Effect>> PENDING_EFFECTS = ThreadLocal.withInitial(LinkedHashSet::new);

    private BatchManager() {
    }

    public static void run(Runnable runnable) {
        var depth = DEPTH.get();
        DEPTH.set(depth + 1);

        try {
            runnable.run();
        } finally {
            DEPTH.set(depth);
            if (depth == 0) {
                flush();
            }
        }
    }

    private static void flush() {
        var effects = PENDING_EFFECTS.get();

        while (!effects.isEmpty()) {
            var snapshot = List.copyOf(effects);
            effects.clear();

            for (var effect : snapshot) {
                try {
                    effect.run();
                } catch (Throwable ex) {
                    // TODO: Log the message
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void schedule(Effect effect) {
        if (DEPTH.get() > 0) {
            PENDING_EFFECTS.get().add(effect);
        } else {
            try {
                effect.run();
            } catch (Throwable ex) {
                // TODO: Log the message
                ex.printStackTrace();
            }
        }
    }
}

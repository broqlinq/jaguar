package io.github.broqlinq.jaguar.reactive.internal.signal;

import io.github.broqlinq.jaguar.reactive.internal.ConfinedScope;
import io.github.broqlinq.jaguar.reactive.signal.Effect;

import java.util.Objects;

public final class ReactiveEffect extends EffectBase implements Effect {

    private final Runnable effect;

    public ReactiveEffect(ConfinedScope scope, Runnable effect) {
        super(scope);
        this.effect = Objects.requireNonNull(effect, "effect");
    }

    @Override
    protected void runEffect() {
        effect.run();
    }
}

package io.github.broqlinq.jaguar.reactive.internal;

import io.github.broqlinq.jaguar.reactive.internal.signal.EffectBase;

import java.util.Arrays;

public abstract class ReactiveNode extends ScopeResource {

    protected static final byte DEPENDENT_SIGNAL = 1;
    protected static final byte DEPENDENT_EFFECT = 2;

    /*
     * Producer side:
     *
     * This node is the producer.
     * These arrays contain consumers that depend on this producer.
     */

    private ReactiveNode[] signalDependents;
    private int[] signalDependentSlots;
    private int signalDependentCount;

    private EffectBase[] effectDependents;
    private int[] effectDependentSlots;
    private int effectDependentCount;

    /*
     * Consumer side:
     *
     * This node is the consumer.
     * These arrays contain producers this node depends on.
     */

    private ReactiveNode[] dependencies;
    protected int[] dependencySlots;
    private byte[] dependencyKinds;
    private long[] dependencySeenVersions;
    private long[] dependencySeenEpochs;
    private int dependencyCount;

    /*
     * Producer version:
     * Incremented when this node's observable value definitely changes.
     */
    protected long version;

    /*
     * Consumer epoch:
     * Incremented before this node evaluates as a consumer.
     * Dependencies read during that evaluation are marked with this epoch.
     */
    protected long epoch;

    protected ReactiveNode(ConfinedScope scope) {
        super(scope);
    }

    /*
     * Only consumer nodes are ever installed as scope.tracked.
     *
     * ComputedSignalBase returns DEPENDENT_SIGNAL.
     * EffectBase returns DEPENDENT_EFFECT.
     * StateSignalBase should never be tracked as a consumer.
     */
    protected byte dependentKind() {
        throw new IllegalStateException("Node is not a dependency consumer");
    }

    /*
     * Producer validation hook.
     *
     * State: no-op.
     * Computed: validate/recompute if stale/invalid.
     * Effect: should never be used as producer.
     */
    protected void validate() {
    }

    /*
     * Consumer invalidation hooks.
     *
     * Computed: mark invalid/stale and propagate.
     * Effect: mark invalidated and schedule.
     */
    protected void onDependencyInvalidated() {
    }

    protected void onDependencyStale() {
        onDependencyInvalidated();
    }

    protected final void trackRead() {
        scope.track(this);
    }

    protected final void beginTracking() {
        epoch++;
    }

    protected final void trackDependency(ReactiveNode producer) {
        int dependencySlot = findDependency(producer);

        if (dependencySlot >= 0) {
            markDependencySeen(dependencySlot, producer);
            return;
        }

        byte kind = dependentKind();

        int producerSlot = producer.addDependent(this, kind, -1);
        dependencySlot = addDependency(producer, producerSlot, kind);

        producer.patchDependentSlot(kind, producerSlot, dependencySlot);

        markDependencySeen(dependencySlot, producer);
    }

    private void markDependencySeen(int dependencySlot, ReactiveNode producer) {
        dependencySeenVersions[dependencySlot] = producer.version;
        dependencySeenEpochs[dependencySlot] = epoch;
    }

    private int findDependency(ReactiveNode producer) {
        var dependencies = this.dependencies;

        if (dependencies == null) {
            return -1;
        }

        for (int i = 0; i < dependencyCount; i++) {
            if (dependencies[i] == producer) {
                return i;
            }
        }

        return -1;
    }

    private int addDependency(ReactiveNode producer, int producerSlot, byte kind) {
        ensureDependencyCapacity();

        int slot = dependencyCount++;

        dependencies[slot] = producer;
        dependencySlots[slot] = producerSlot;
        dependencyKinds[slot] = kind;
        dependencySeenVersions[slot] = producer.version;
        dependencySeenEpochs[slot] = epoch;

        return slot;
    }

    private int addDependent(ReactiveNode consumer, byte kind, int consumerSlot) {
        return switch (kind) {
            case DEPENDENT_SIGNAL -> addSignalDependent(consumer, consumerSlot);
            case DEPENDENT_EFFECT -> addEffectDependent((EffectBase) consumer, consumerSlot);
            default -> throw new IllegalStateException("Unknown dependent kind: " + kind);
        };
    }

    private void patchDependentSlot(byte kind, int producerSlot, int consumerSlot) {
        switch (kind) {
            case DEPENDENT_SIGNAL -> signalDependentSlots[producerSlot] = consumerSlot;
            case DEPENDENT_EFFECT -> effectDependentSlots[producerSlot] = consumerSlot;
            default -> throw new IllegalStateException("Unknown dependent kind: " + kind);
        }
    }

    private int addSignalDependent(ReactiveNode consumer, int consumerSlot) {
        ensureSignalDependentCapacity();

        int slot = signalDependentCount++;

        signalDependents[slot] = consumer;
        signalDependentSlots[slot] = consumerSlot;

        return slot;
    }

    private int addEffectDependent(EffectBase effect, int consumerSlot) {
        ensureEffectDependentCapacity();

        int slot = effectDependentCount++;

        effectDependents[slot] = effect;
        effectDependentSlots[slot] = consumerSlot;

        return slot;
    }

    protected final boolean validateDependenciesAndCheckChanged() {
        boolean changed = false;

        for (int i = 0; i < dependencyCount; i++) {
            ReactiveNode producer = dependencies[i];

            producer.validate();

            if (producer.version != dependencySeenVersions[i]) {
                changed = true;
            }
        }

        return changed;
    }

    protected final void removeStaleDependencies() {
        for (int i = dependencyCount - 1; i >= 0; i--) {
            if (dependencySeenEpochs[i] != epoch) {
                unlinkDependencyAt(i);
            }
        }
    }

    protected final void unlinkAllDependencies() {
        for (int i = dependencyCount - 1; i >= 0; i--) {
            unlinkDependencyAt(i);
        }
    }

    protected final void unlinkAllDependents() {
        for (int i = signalDependentCount - 1; i >= 0; i--) {
            unlinkSignalDependentAt(i);
        }

        for (int i = effectDependentCount - 1; i >= 0; i--) {
            unlinkEffectDependentAt(i);
        }
    }

    private void unlinkDependencyAt(int dependencySlot) {
        ReactiveNode producer = dependencies[dependencySlot];
        int producerSlot = dependencySlots[dependencySlot];
        byte kind = dependencyKinds[dependencySlot];

        removeDependencyAt(dependencySlot);

        switch (kind) {
            case DEPENDENT_SIGNAL -> producer.removeSignalDependentAt(producerSlot);
            case DEPENDENT_EFFECT -> producer.removeEffectDependentAt(producerSlot);
            default -> throw new IllegalStateException("Unknown dependent kind: " + kind);
        }
    }

    private void unlinkSignalDependentAt(int producerSlot) {
        ReactiveNode consumer = signalDependents[producerSlot];
        int consumerSlot = signalDependentSlots[producerSlot];

        removeSignalDependentAt(producerSlot);
        consumer.removeDependencyAt(consumerSlot);
    }

    private void unlinkEffectDependentAt(int producerSlot) {
        EffectBase consumer = effectDependents[producerSlot];
        int consumerSlot = effectDependentSlots[producerSlot];

        removeEffectDependentAt(producerSlot);
        consumer.removeDependencyAt(consumerSlot);
    }

    protected void removeDependencyAt(int index) {
        int last = --dependencyCount;

        ReactiveNode movedProducer = dependencies[last];
        int movedProducerSlot = dependencySlots[last];
        byte movedKind = dependencyKinds[last];
        long movedSeenVersion = dependencySeenVersions[last];
        long movedSeenEpoch = dependencySeenEpochs[last];

        dependencies[last] = null;
        dependencySlots[last] = 0;
        dependencyKinds[last] = 0;
        dependencySeenVersions[last] = 0L;
        dependencySeenEpochs[last] = 0L;

        if (index == last) {
            return;
        }

        dependencies[index] = movedProducer;
        dependencySlots[index] = movedProducerSlot;
        dependencyKinds[index] = movedKind;
        dependencySeenVersions[index] = movedSeenVersion;
        dependencySeenEpochs[index] = movedSeenEpoch;

        movedProducer.patchDependentSlot(movedKind, movedProducerSlot, index);
    }

    private void removeSignalDependentAt(int index) {
        int last = --signalDependentCount;

        ReactiveNode movedConsumer = signalDependents[last];
        int movedConsumerSlot = signalDependentSlots[last];

        signalDependents[last] = null;
        signalDependentSlots[last] = 0;

        if (index == last) {
            return;
        }

        signalDependents[index] = movedConsumer;
        signalDependentSlots[index] = movedConsumerSlot;

        movedConsumer.dependencySlots[movedConsumerSlot] = index;
    }

    private void removeEffectDependentAt(int index) {
        int last = --effectDependentCount;

        EffectBase movedConsumer = effectDependents[last];
        int movedConsumerSlot = effectDependentSlots[last];

        effectDependents[last] = null;
        effectDependentSlots[last] = 0;

        if (index == last) {
            return;
        }

        effectDependents[index] = movedConsumer;
        effectDependentSlots[index] = movedConsumerSlot;

        movedConsumer.dependencySlots[movedConsumerSlot] = index;
    }

    protected final void notifySignalDependentsInvalidated() {
        for (int i = 0; i < signalDependentCount; i++) {
            signalDependents[i].onDependencyInvalidated();
        }
    }

    protected final void notifySignalDependentsStale() {
        for (int i = 0; i < signalDependentCount; i++) {
            signalDependents[i].onDependencyStale();
        }
    }

    protected final void notifyEffectDependents() {
        for (int i = 0; i < effectDependentCount; i++) {
            effectDependents[i].onDependencyInvalidated();
        }
    }

    protected final void notifyDependentsInvalidated() {
        notifySignalDependentsInvalidated();
        notifyEffectDependents();
    }

    protected final void notifyDependentsStale() {
        notifySignalDependentsStale();
        notifyEffectDependents();
    }

    protected final void commitProducerChange() {
        version++;
        notifyDependentsInvalidated();
    }

    private void ensureDependencyCapacity() {
        if (dependencies == null) {
            dependencies = new ReactiveNode[4];
            dependencySlots = new int[4];
            dependencyKinds = new byte[4];
            dependencySeenVersions = new long[4];
            dependencySeenEpochs = new long[4];
            return;
        }

        if (dependencyCount == dependencies.length) {
            int newLength = dependencies.length << 1;

            dependencies = Arrays.copyOf(dependencies, newLength);
            dependencySlots = Arrays.copyOf(dependencySlots, newLength);
            dependencyKinds = Arrays.copyOf(dependencyKinds, newLength);
            dependencySeenVersions = Arrays.copyOf(dependencySeenVersions, newLength);
            dependencySeenEpochs = Arrays.copyOf(dependencySeenEpochs, newLength);
        }
    }

    private void ensureSignalDependentCapacity() {
        if (signalDependents == null) {
            signalDependents = new ReactiveNode[4];
            signalDependentSlots = new int[4];
            return;
        }

        if (signalDependentCount == signalDependents.length) {
            int newLength = signalDependents.length << 1;

            signalDependents = Arrays.copyOf(signalDependents, newLength);
            signalDependentSlots = Arrays.copyOf(signalDependentSlots, newLength);
        }
    }

    private void ensureEffectDependentCapacity() {
        if (effectDependents == null) {
            effectDependents = new EffectBase[2];
            effectDependentSlots = new int[2];
            return;
        }

        if (effectDependentCount == effectDependents.length) {
            int newLength = effectDependents.length << 1;

            effectDependents = Arrays.copyOf(effectDependents, newLength);
            effectDependentSlots = Arrays.copyOf(effectDependentSlots, newLength);
        }
    }

    @Override
    protected void onDispose() {
        unlinkAllDependencies();
        unlinkAllDependents();
    }
}

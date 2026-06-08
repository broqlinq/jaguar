package io.github.broqlinq.jaguar.reactive.internal;

abstract sealed class ProducerNode extends ReactiveNode implements Producer permits ScopedIntState, ScopedState {

    protected final ConsumerList computedLinks = new ConsumerList(this);
    protected final ConsumerList effectLinks = new ConsumerList(this);
    protected long version;

    protected ProducerNode(ThreadConfinedScope scope) {
        super(scope);
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public ConsumerList computedLinks() {
        return computedLinks;
    }

    protected final void updateVersion() {
        version++;
    }

    protected final void notifyDependents() {
        computedLinks.markDependentsInvalid();
    }

    @Override
    protected void onDispose() {
        computedLinks.unlinkAll();
    }
}

package io.github.broqlinq.jaguar.reactive.internal;

abstract sealed class ProducerConsumerNode extends ConsumerNode implements Producer permits ScopedComputed, ScopedIntComputed {

    protected final ConsumerList computedLinks = new ConsumerList(this);
    protected final ConsumerList effectLinks = new ConsumerList(this);

    protected long version;

    protected ProducerConsumerNode(ThreadConfinedScope scope, Consumer.Type type) {
        super(scope, type);
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public ConsumerList computedLinks() {
        return computedLinks;
    }

    @Override
    public ConsumerList effectLinks() {
        return effectLinks;
    }

    protected final void updateVersion() {
        version++;
    }

    @Override
    protected void onDispose() {
        computedLinks.unlinkAll();
    }

}

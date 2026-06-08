package io.github.broqlinq.jaguar.reactive.internal;

abstract sealed class ConsumerNode extends ReactiveNode implements Consumer permits ProducerConsumerNode, ScopedEffect {

    protected final Consumer.Type type;
    protected final ProducerList producerLinks = new ProducerList(this);
    protected long epoch;
    protected boolean tracking;
    protected Consumer.State state;

    protected ConsumerNode(ThreadConfinedScope scope, Type type) {
        super(scope);
        this.type = type;
    }

    @Override
    public long epoch() {
        return epoch;
    }

    @Override
    public ProducerList producerLinks() {
        return producerLinks;
    }

    protected void updateEpoch() {
        epoch++;
    }

    @Override
    public boolean isTracking() {
        return tracking;
    }

    @Override
    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }

}

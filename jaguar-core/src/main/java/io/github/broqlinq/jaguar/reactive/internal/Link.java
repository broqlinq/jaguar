package io.github.broqlinq.jaguar.reactive.internal;

final class Link {

    static final Link[] EMPTY_LIST = new Link[0];

    Producer producer;
    Consumer consumer;

    long lastVersion;
    long lastEpoch;

    int indexInProducer;
    int indexInConsumer;

    boolean active;

    Link() {
        clear();
    }

    void markInvalid() {
        if (producer.version() != lastVersion) {
            consumer.markInvalid();
        }
    }

    public void markStale() {
        consumer.markStale();
    }

    void markSeen() {
        this.lastVersion = producer.version();
        this.lastEpoch = consumer.epoch();
    }

    public void link(Producer producer, Consumer consumer) {
        this.producer = producer;
        this.consumer = consumer;

        producer.computedLinks().add(this);
        consumer.producerLinks().add(this);

        markSeen();

        active = true;
    }

    public void unlink() {
        if (!active) {
            return;
        }

        producer.computedLinks().remove(this);
        consumer.producerLinks().remove(this);

        clear();
    }

    public void clear() {
        active = false;

        producer = null;
        consumer = null;

        lastVersion = -1;
        lastEpoch = -1;

        indexInProducer = -1;
        indexInConsumer = -1;
    }

    @Override
    public String toString() {
        return "Link[" + consumer + " (" + indexInConsumer + ") -> " + producer + " (" + indexInProducer + ")]";
    }
}

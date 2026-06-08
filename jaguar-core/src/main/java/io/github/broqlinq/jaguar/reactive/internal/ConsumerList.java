package io.github.broqlinq.jaguar.reactive.internal;

import java.util.Arrays;

final class ConsumerList {

    private final Producer producer;

    private Link[] links = Link.EMPTY_LIST;
    private int size = 0;

    public ConsumerList(Producer producer) {
        this.producer = producer;
    }

    public void add(Link link) {
        if (updateIfPresent(link)) {
            return;
        }

        final int len = links.length;
        final int index = this.size;

        if (len == 0) {
            links = new Link[2];
        } else if (index == len) {
            int growth = len < 64 ? len : len >> 1;
            links = Arrays.copyOf(links, len + growth);
        }

        links[index] = link;
        this.size = index + 1;
        link.producer = producer;
        link.indexInProducer = index;
    }

    private boolean updateIfPresent(Link link) {
        final int index = link.indexInProducer;
        if (index == -1) {
            return false;
        }
        if (links[index] != link) {
            throw new IllegalArgumentException("Corrupted link list in producer");
        }
        link.markSeen();
        return true;
    }

    public void remove(Link link) {
        final int size = this.size;
        final int index = link.indexInProducer;
        var removed = links[index];
        if (removed != link) {
            throw new IllegalArgumentException("Corrupted link list in producer");
        }
        var moved = links[size - 1];

        links[size - 1] = null;

        if (index != size - 1) {
            links[index] = moved;
            moved.indexInProducer = index;
        }

        this.size = size - 1;
    }

    public boolean nonEmpty() {
        return size != 0;
    }

    public void markDependentsInvalid() {
        for (int i = 0; i < size; i++) {
            links[i].markInvalid();
        }
    }

    public void unlinkAll() {
        for (int i = size - 1; i >= 0; i--) {
            var link = links[i];
            link.unlink();
        }
    }

    public void markDependentsStale() {
        for (int i = 0; i < size; i++) {
            links[i].markStale();
        }
    }
}

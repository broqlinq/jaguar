package io.github.broqlinq.jaguar.reactive.internal;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.IdentityHashMap;

final class ProducerList {

    private final ArrayDeque<Link> pool = new ArrayDeque<>();
    private final IdentityHashMap<Producer, Link> producerLinks = new IdentityHashMap<>();
    private Link[] links = Link.EMPTY_LIST;
    private int size = 0;

    private final Consumer consumer;

    public ProducerList(Consumer consumer) {
        this.consumer = consumer;
        for (int i = 0; i < 128; i++) {
            pool.offer(new Link());
        }
    }

    public Link getOrCreate(Producer producer) {
        Link link;
        if (size > 32) {
            link = producerLinks.get(producer);
        } else {
            link = null;
            for (int i = 0; i < size; i++) {
                if (links[i].producer == producer) {
                    link = links[i];
                    break;
                }
            }
        }
        if (link != null) {
            return link;
        }

        link = pool.poll();
        if (link == null) {
            link = new Link();
        }
        return link;
    }

    public void add(Link link) {
        if (updateIfPresent(link)) {
            return;
        }

        final int len = links.length;
        final int size = this.size;

        if (size == len) {
            int length = len == 0 ? 4 : len + (len >> 1);
            links = Arrays.copyOf(links, length);
        }

        links[size] = link;
        this.size = size + 1;
        link.consumer = consumer;
        link.indexInConsumer = size;
        producerLinks.put(link.producer, link);
    }

    private boolean updateIfPresent(Link link) {
        final int index = link.indexInConsumer;
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
        final int index = link.indexInConsumer;
        var removed = links[index];
        if (removed != link) {
            throw new IllegalArgumentException("Corrupted link list in consumer: " + link);
        }
        var moved = links[size - 1];

        links[size - 1] = null;

        if (index != size - 1) {
            links[index] = moved;
            moved.indexInConsumer = index;
        }
        this.size = size - 1;
        producerLinks.remove(link.producer);
    }

    public void removeStale() {
        final int len = size;
        for (int i = len - 1; i >= 0; i--) {
            var link = links[i];
            if (link.lastEpoch != consumer.epoch()) {
//                links[i] = null;
                producerLinks.remove(link.producer);
                link.unlink();
                pool.offer(link);
            }
        }

    }

    public void unlinkAll() {
        for (int i = size - 1; i >= 0; i--) {
            links[i].unlink();
        }
    }

    public boolean detectChanged() {
        for (int i = 0; i < size; i++) {
            var link = links[i];
            var producer = link.producer;
            if (producer instanceof ScopedComputed<?> c) {
                c.validate();
            }

            if (producer.version() != link.lastVersion) {
                return true;
            }
        }
        return false;
    }
}

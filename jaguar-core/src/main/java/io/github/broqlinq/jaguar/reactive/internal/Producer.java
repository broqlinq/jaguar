package io.github.broqlinq.jaguar.reactive.internal;

interface Producer {

    long version();

    ConsumerList computedLinks();

    ConsumerList effectLinks();
}

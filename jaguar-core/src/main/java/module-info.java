module jaguar.core {
    requires java.desktop;
    requires jdk.httpserver;

    exports io.github.broqlinq.jaguar.reactive;
    exports io.github.broqlinq.jaguar.reactive.internal;
}
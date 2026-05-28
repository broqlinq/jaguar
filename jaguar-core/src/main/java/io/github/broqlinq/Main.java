package io.github.broqlinq;

import io.github.broqlinq.jaguar.core.signal.State;

public class Main {

    static void main() {
        State<Number> state = null;

        state.update(Main::foo);
    }

    static Integer foo(Object o) {
        return 5;
    }
}
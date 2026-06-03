package io.github.broqlinq;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static void main() {

        effect();
    }

    static Effect effect() {
        return new Effect();
    }

    static class Effect implements AutoCloseable {

        @Override
        public void close() {

        }
    }
}
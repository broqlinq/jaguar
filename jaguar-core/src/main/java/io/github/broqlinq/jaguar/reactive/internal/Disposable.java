package io.github.broqlinq.jaguar.reactive.internal;

public interface Disposable {

    static Disposable NULL = new Disposable() {

        @Override
        public boolean isDisposed() {
            return true;
        }

        @Override
        public void dispose() {
        }
    };

    boolean isDisposed();

    void dispose();

}

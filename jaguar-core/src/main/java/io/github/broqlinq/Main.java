package io.github.broqlinq;

import io.github.broqlinq.jaguar.core.util.Equivalence;

public class Main {

    static void main() {

        var person1 = new Person("Alice", "Smith", 29);
        var person2 = new Person("John", "Smith", 32);

        areEqual(person1, person2, Equivalence.by(Person::firstName).and(Person::lastName));
    }

    static <T> boolean areEqual(T t1, T t2, Equivalence<? super T> equivalence) {
        return equivalence.areEqual(t1, t2);
    }

    record Person(String firstName, String lastName, int age) {}
}
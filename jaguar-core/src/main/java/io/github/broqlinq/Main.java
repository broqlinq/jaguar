package io.github.broqlinq;

import io.github.broqlinq.jaguar.reactive.signal.Signal;
import io.github.broqlinq.jaguar.reactive.signal.State;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Main {

    static void main() {

        Signal<String> signal = newSignal();

        State<Person> person = newSignal();

        var address = person.project(Person::address, Person::withAddress);

        address.set(new Address("Foo", "Bar", "Buz"));

        Function<Person, Address> extractAddress = Person::address;

        BiFunction<Person, Address, Person> combiner = Person::withAddress;
    }

    static <T, S extends Signal<T>> S newSignal() {
        throw new UnsupportedOperationException();
    }

    record Person(Name name, Address address) {

        Person withAddress(Address address) {
            return new Person(name, address);
        }

        Person withName(Name name) {
            return new Person(name, address);
        }
    }

    record Address(String country, String city, String street) {}

    record Name(String firstName, String lastName) {}
}
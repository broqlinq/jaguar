# Jaguar Core (WIP)

A core module for Jaguar GUI. This is a part of experimental Java GUI framework.

## Reactive Core

Reactive Core defines public API for reactive signals (inspired by existing JS frameworks).
It includes generic and primitive signal specializations, as well as side effects.

### Signals

Signals are imagined as primitive UI state building blocks. 
In general, `Signal<T>` and its primitive specializations are **read-only**, while `State<T>` and its primitive specializations
are **mutable**.

Signals can be derived from another signals. Dependency management and tracking
is automatic, while results are cached by default to prevent unnecessary updates.
In case of computed/derived signals, they remain invalidated and are recomputed lazily.

Generic states and computed signals use `Object::equals` by default to check whether the value
has actually changed, but this is behavior is configurable by passing an optional predicate function.\
In case of primitive states and signals, their values are always compared by `==` operator.

#### Example

```java
import io.github.broqlinq.jaguar.reactive.signal.*;

public class Foo {

    // Mutable int counter
    private final IntState counter;
    
    // Message derived from counter
    private final Signal<String> message;
    
    public Foo(IntState counter) {
        this.counter = counter;
        
        // Message is updated every time counter changes
        this.message = counter.derive(val -> "Counter is " + value);
    }
    
    public void increment() {
        counter.update(v -> v + 1);
    }
    
    public String message() {
        return message.value();
    }
}
```

### Reactive Context and Scopes

Signals are not meant to exist in vacuum. They are explicitly created by **Reactive Context**, 
while their lifetime is managed by **Scope**.



Current implementation supports only thread-confined scopes (defined in `jaguar.reactive.internal.ThreadConfinedScope`).

#### Example 

```java
void main() {
    
    try (var scope = newScope()) {
        // Signals live as long as scope is open
        var quantity = scope.state(2);
        var unitPrice = scope.state(10);
        
        // total is derived from quantity and unitPrice, and changes
        // its value whenever any dependency changes
        var total = scope.compute(() -> quantity.value() + unitPrice.value());
        
        // Dependencies are automatically rediscovered
        var message = scope.compute(() -> {
            if (quantity.value() > 5) {
                return "More that 5 items";
            } else {
                // unitPrice will not affect the message
                // if the condition above is satisfied
                return "Less than 5 items at price " + unitPrice.value();
            }
        });
        
        quantity.set(3);
    }
}

static Scope newScope() {
    // Creates a new scope confined to the current thread
    return new ConfinedScope(new SchedulerImpl(Thread.currentThread()));
}
```

Scopes also provide a way to run multiple signal updates efficiently using `Scope::batch`.
Batching multiple signal changes defers update of dependent signals or effects until the end.

#### Example

```java
import io.github.broqlinq.jaguar.reactive.*;

void main() {
    try (var scope = newScope()) {
        var x = scope.intState(1);
        var y = scope.intState(4);
        
        // derives a signal as sum of values of x and y signals
        // it's not invoked until the value is actually needed
        var sum = scope.computed((IntSupplier) () -> x.value() + y.value());
        
        // prints 5
        IO.println(sum.value());
        
        // updates x and y values 'at once'
        scope.batch(() -> {
           x.set(5);
           y.set(7);
        });
        
        // prints 12
        IO.println(sum.value());
    }
}
```

### Effects

`Effect` allows side effects to exists. Effects are run once at creation, and every time
any dependency updates, similar to computed/derived signals.

#### Example

```java
import io.github.broqlinq.jaguar.reactive.signal.IntState;

void main() {
    try (var scope = newScope()) {
        var x = scope.intState(1);
        var y = scope.intState(4);

        // prints 5 initially
        var _ = scope.effect(() -> IO.println(x.value() + y.value()));

        // updates x and y values in a batch
        // effect is rerun only once
        // prints 12
        scope.batch(() -> {
            x.set(5);
            y.set(7);
        });
    }
}
```

### Projections (TODO)

Generic state signals provide a way to 'project its value' onto another mutable state.
In principle, projected state is bound to the original state and changes to either updates the other one.

#### Example

```java

record Address(String country, String city, String street, String postalCode) {
    Address withStreet(String street) {
        return new Address(country, city, street, postalCode);
    }
}

record Person(String firstName, String lastName, Address address) {
    Person withFirstName(String firstName) {
        return new Person(firstName, lastName, address);
    }
    
    Person withAddress(Address address) {
        return new Person(firstName, lastName, address);
    }
}

void main() {
    try (var scope = openScope()) {
        // person state contains all person data
        var person = scope.state(new Person(
                "Mark",
                "Smith",
                new Address(
                        "England",
                        "London",
                        "Oak Street 12",
                        "123456"
                )
        ));
        
        // address state represents address of the person
        // from the state above
        var address = person.project(
                Person::address,        // a way to extract address from person 
                Person::withAddress     // a way to reconstruct person with new address
        );
        
        // Prints person with old address
        IO.println(person.value());
        
        address.update(add -> add.withStreet("New Street 123"));
        
        // Prints person with updated address
        IO.println(person.value());
        
        person.update(per -> per.withFirstName("John"));
        
        // Address remains the same
        IO.println(address.value());
    }
}
```
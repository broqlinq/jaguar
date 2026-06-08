package io.github.broqlinq;

import io.github.broqlinq.jaguar.reactive.Scope;
import io.github.broqlinq.jaguar.reactive.Signal;
import io.github.broqlinq.jaguar.reactive.State;
import io.github.broqlinq.jaguar.reactive.internal.ThreadConfinedScheduler;
import io.github.broqlinq.jaguar.reactive.internal.ThreadConfinedScope;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final Path RESOURCES_PATH = Path.of("C:\\Users\\broql\\IdeaProjects\\jaguar\\jaguar-core\\src\\main\\resources");
    private static final Path REACTIVE_PACKAGE_PATH = Path.of("C:\\Users\\broql\\IdeaProjects\\jaguar\\jaguar-core\\src\\main\\java\\io\\github\\broqlinq\\jaguar\\reactive");

    static void main() throws IOException {

        test2();
    }

    static void test2() {
        Scope scope = new ThreadConfinedScope(
                "test",
                new ThreadConfinedScheduler(Thread.currentThread())
        );

        State<Integer> source = scope.state(0, Objects::equals);

        AtomicInteger parityRuns = new AtomicInteger();
        AtomicInteger derivedRuns = new AtomicInteger();
        AtomicInteger effectRuns = new AtomicInteger();

        Signal<Integer> parity = scope.computed(() -> {
            parityRuns.incrementAndGet();
            return source.value() & 1;
        }, Objects::equals);

        Signal<Integer> derived = scope.computed(() -> {
            derivedRuns.incrementAndGet();
            return parity.value() + 1;
        }, Objects::equals);

        scope.effect(() -> {
            effectRuns.incrementAndGet();
            derived.value();
        });

        int parityInitial = parityRuns.get();
        int derivedInitial = derivedRuns.get();
        int effectInitial = effectRuns.get();

        source.set(2);

        IO.println(parityInitial + ", " + parityRuns.get());
        IO.println(derivedInitial +  ", " + derivedRuns.get());
        IO.println(effectInitial +  ", " + effectRuns.get());

        scope.close();
    }

    static void test() {
        try (var scope = ThreadConfinedScope.create("scope")) {
            State<Integer> count = scope.state(2, Objects::equals);

            Signal<Integer> doubled = scope.computed(() -> count.value() * 2);
            Signal<String> label = scope.computed(() -> "Value: " + doubled.value());
            IO.println("Value: 4, actual: " + label.value());

            count.set(3);

            IO.println("Value: 6, actual: " + label.value());
        }

    }

    private static void writeCode() throws IOException {
        IO.println(Path.of(".").toAbsolutePath());
        var outputDirectory = RESOURCES_PATH.resolve("output");
        Files.createDirectories(outputDirectory);

        var outputFile = outputDirectory.resolve("output.txt");
        try (var is = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE)) {
            Files.walkFileTree(REACTIVE_PACKAGE_PATH, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    is.write("// Begin " + file.getFileName() + "\n\n");
                    try (var fis = Files.newBufferedReader(file)) {
                        String line;
                        while ((line = fis.readLine()) != null) {
                            is.write(line);
                            is.write("\n");
                            is.flush();
                        }
                    }
                    is.write("\n// End " + file.getFileName() + "\n\n");
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

}
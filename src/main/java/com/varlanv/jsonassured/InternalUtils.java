package com.varlanv.jsonassured;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface InternalUtils {

    static <T extends Throwable, R> R rethrow(Throwable t) throws T {
        @SuppressWarnings("unchecked")
        var res = (T) t;
        throw res;
    }

    static <T> T sneakyGet(JsonAssured.ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return rethrow(e);
        }
    }

    static <T, R> R isIn(T actual, Iterable<T> expected, R response) {
        for (var n : expected) {
            if (n.equals(actual)) {
                return response;
            }
        }
        throw new AssertionError("");
    }


    static <R> R hasSize(R toReturn,
                         Supplier<? extends List<?>> subjectSupplier,
                         int expectedSize,
                         String path,
                         String arrayType) {
        var subject = subjectSupplier.get();
        if (subject.size() == expectedSize) {
            return toReturn;
        }
        throw new AssertionError(String.format("%s array at path \"%s\" has size %d, but expected size is %d",
                arrayType, path, subject.size(), expectedSize));
    }

    static <E, R> R containsAll(R toReturn,
                                Supplier<? extends List<E>> subjectSupplier,
                                Stream<E> expected,
                                String path,
                                String arrayType) {
        var subject = subjectSupplier.get();
        var expectedList = streamToList(expected);
        if (subject.isEmpty()) {
            return toReturn;
        }
        for (var actual : subject) {
            var found = false;
            for (var expectedVal : expectedList) {
                if (actual.equals(expectedVal)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                return toReturn;
            }
        }
        throw new AssertionError(String.format("%s array at path \"%s\" does not contain some of expected values",
                arrayType, path));
    }

    static <E, R> R containsAny(R toReturn,
                                Supplier<? extends List<E>> subjectSupplier,
                                Stream<E> expected,
                                String path,
                                String arrayType) {
        var subject = subjectSupplier.get();
        var expectedList = streamToList(expected);
        if (subject.isEmpty()) {
            return toReturn;
        }
        for (var actual : subject) {
            for (var expectedVal : expectedList) {
                if (actual.equals(expectedVal)) {
                    return toReturn;
                }
            }
        }
        throw new AssertionError(String.format("%s array at path \"%s\" does not contain any of expected values",
                arrayType, path));
    }

    static <E, R> R allSatisfy(R toReturn,
                               Supplier<? extends List<E>> subjectSupplier,
                               JsonAssured.ThrowingConsumer<E> consumer) {
        var subject = subjectSupplier.get();
        for (var actual : subject) {
            try {
                consumer.toUnchecked().accept(actual);
            } catch (Throwable e) {
                throw new AssertionError("Not satisfied", e);
            }
        }
        return toReturn;
    }

    static <E, R> R anySatisfy(R toReturn,
                               Supplier<? extends List<E>> subjectSupplier,
                               JsonAssured.ThrowingConsumer<E> consumer) {
        var subject = subjectSupplier.get();
        for (var actual : subject) {
            try {
                consumer.toUnchecked().accept(actual);
                return toReturn;
            } catch (Throwable e) {
                throw new AssertionError("Not satisfied", e);
            }
        }
        return toReturn;
    }

    static <E, R> R satisfy(R toReturn,
                            Supplier<? extends List<E>> subjectSupplier,
                            JsonAssured.ThrowingConsumer<List<E>> consumer) {
        var subject = subjectSupplier.get();
        consumer.toUnchecked().accept(subject);
        return toReturn;
    }

    static String formatActualExpected(Object actual, Object expected) {
        return String.format("Expected: <%s> but was: <%s>", expected, actual);
    }

    static <E> List<E> streamToList(Stream<E> expected) {
        var counter = new int[1];
        var expectedList = expected
                .peek(it -> {
                    if (it == null) {
                        throw new IllegalArgumentException("Array of expected values cannot contain null elements, " +
                                "but found null element at index " + counter[0]);
                    }
                    counter[0]++;
                })
                .collect(Collectors.toList());
        if (expectedList.isEmpty()) {
            throw new IllegalArgumentException("Array of expected values cannot be empty");
        }
        return expectedList;
    }
}

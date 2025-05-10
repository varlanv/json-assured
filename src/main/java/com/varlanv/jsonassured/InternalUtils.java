package com.varlanv.jsonassured;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

interface InternalUtils {

  static <T, R> R satisfies(
      JsonAssured.ThrowingConsumer<T> action,
      Supplier<T> actualSupplier,
      R toReturn,
      String typeName,
      String path) {
    try {
      action.toUnchecked().accept(actualSupplier.get());
    } catch (Throwable t) {
      InternalUtils.rethrowUnrecoverable(t);
      throw new AssertionError(
          String.format(
              "%s value at path \"%s\" did not satisfy provided condition", typeName, path),
          t);
    }
    return toReturn;
  }

  static void expectedNotNull(Object expected) {
    if (expected == null) {
      throw new IllegalArgumentException("Expected value cannot be null");
    }
  }

  static void expectedNotNull(Object expected, String field) {
    if (expected == null) {
      throw new IllegalArgumentException(String.format("'%s' value cannot be null", field));
    }
  }

  static <T extends Throwable, R> R rethrow(Throwable exception) throws T {
    @SuppressWarnings("unchecked")
    var res = (T) exception;
    throw res;
  }

  static void rethrowUnrecoverable(Throwable exception) {
    if (exception instanceof OutOfMemoryError) {
      rethrow(exception);
    }
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

  static <R> R isEmpty(
      R toReturn, Supplier<? extends List<?>> subjectSupplier, String path, String arrayType) {
    var subject = subjectSupplier.get();
    if (subject.isEmpty()) {
      return toReturn;
    }
    throw new AssertionError(
        String.format(
            "%s array at path \"%s\" has size %d, but expected to be empty",
            arrayType, path, subject.size()));
  }

  static <R> R isNotEmpty(
      R toReturn, Supplier<? extends List<?>> subjectSupplier, String path, String arrayType) {
    var subject = subjectSupplier.get();
    if (!subject.isEmpty()) {
      return toReturn;
    }
    throw new AssertionError(
        String.format(
            "%s array at path \"%s\" is expected to be not empty, but was empty", arrayType, path));
  }

  static <R> R hasSize(
      R toReturn,
      Supplier<? extends List<?>> subjectSupplier,
      int expectedSize,
      String path,
      String arrayType) {
    var subject = subjectSupplier.get();
    if (subject.size() == expectedSize) {
      return toReturn;
    }
    throw new AssertionError(
        String.format(
            "%s array at path \"%s\" has size %d, but expected size is %d",
            arrayType, path, subject.size(), expectedSize));
  }

  static <R> R doesNotContainNull(
      R toReturn, Supplier<? extends List<?>> subjectSupplier, String path, String arrayType) {
    var subject = subjectSupplier.get();
    if (subject.isEmpty()) {
      return toReturn;
    }
    var indexesOfNull = new ArrayList<Integer>(0);
    for (var idx = 0; idx < subject.size(); idx++) {
      if (subject.get(idx) == null) {
        indexesOfNull.add(idx);
      }
    }
    if (indexesOfNull.isEmpty()) {
      return toReturn;
    } else if (indexesOfNull.size() == 1) {
      throw new AssertionError(
          String.format(
              "%s array at path \"%s\" expected to not contain null, but found one null at index [%d]",
              arrayType, path, indexesOfNull.get(0)));
    } else {
      throw new AssertionError(
          String.format(
              "%s array at path \"%s\" expected to not contain null, but found multiple nulls at indexes [%s]",
              arrayType, path, indexesOfNull));
    }
  }

  static <E, R> R containsAll(
      R toReturn,
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
    throw new AssertionError(
        String.format(
            "%s array at path \"%s\" does not contain some of expected values", arrayType, path));
  }

  static <E, R> R containsAny(
      R toReturn,
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
    throw new AssertionError(
        String.format(
            "%s array at path \"%s\" does not contain any of expected values", arrayType, path));
  }

  static <E, R> R allSatisfy(
      R toReturn,
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

  static <E, R> R anySatisfy(
      R toReturn,
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

  static <E, R> R satisfy(
      R toReturn,
      Supplier<? extends List<E>> subjectSupplier,
      JsonAssured.ThrowingConsumer<List<E>> consumer) {
    var subject = subjectSupplier.get();
    consumer.toUnchecked().accept(subject);
    return toReturn;
  }

  static String formatActualExpected(Object actual, Object expected) {
    return String.format("Expected: <%s> but was: <%s>", expected, actual);
  }

  static <T, R> List<R> listFromIterable(Iterable<T> iterable, Function<T, R> mapper) {
    var objects =
        new ArrayList<R>(iterable instanceof Collection ? ((Collection<?>) iterable).size() : 10);
    var counter = 0;
    for (var t : iterable) {
      if (t == null) {
        throw new IllegalArgumentException(
            "Array of expected values cannot contain null elements, "
                + String.format("but found null element at index [%d]", counter));
      }
      objects.add(mapper.apply(t));
      counter++;
    }
    return objects;
  }

  static <T> List<T> listFromIterable(Iterable<T> iterable) {
    return listFromIterable(iterable, Function.identity());
  }

  static <E> List<E> streamToList(Stream<E> expected) {
    var counter = new int[1];
    var expectedList =
        expected
            .peek(
                it -> {
                  if (it == null) {
                    throw new IllegalArgumentException(
                        "Array of expected values cannot contain null elements, "
                            + "but found null element at index "
                            + counter[0]);
                  }
                  counter[0]++;
                })
            .collect(Collectors.toList());
    if (expectedList.isEmpty()) {
      throw new IllegalArgumentException("Array of expected values cannot be empty");
    }
    return expectedList;
  }

  static <E, T extends Throwable> List<E> objectToList(
      @Nullable Object val, Function<Object, E> mapper, Function<@Nullable Object, T> onError)
      throws T {
    if (val instanceof Iterable<?>) {
      var items = (Iterable<?>) val;
      var objects = new ArrayList<E>();
      for (var item : items) {
        if (item == null) {
          objects.add(null);
        } else {
          objects.add(mapper.apply(item));
        }
      }
      return objects;
    }
    throw onError.apply(val);
  }
}

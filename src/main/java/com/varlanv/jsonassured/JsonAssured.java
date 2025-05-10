package com.varlanv.jsonassured;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

public interface JsonAssured {

  static JsonPathAssertions assertJson(byte[] bytes) {
    Objects.requireNonNull(bytes);
    return new JsonPathAssertions(
        new MemoizedSupplier<>(() -> JsonPath.parse(new ByteArrayInputStream(bytes))));
  }

  static JsonPathAssertions assertJson(InputStream is) {
    Objects.requireNonNull(is);
    return new JsonPathAssertions(new MemoizedSupplier<>(() -> JsonPath.parse(is)));
  }

  static JsonPathAssertions assertJson(Reader reader) {
    Objects.requireNonNull(reader);
    var str =
        new BufferedReader(reader).lines().collect(Collectors.joining(System.lineSeparator()));
    var bytes = str.getBytes(StandardCharsets.UTF_8);
    return assertJson(bytes);
  }

  static JsonPathAssertions assertJson(@Language("json") String json) {
    Objects.requireNonNull(json);
    return new JsonPathAssertions(new MemoizedSupplier<>(() -> JsonPath.parse(json)));
  }

  final class JsonPathAssertions {

    private final MemoizedSupplier<DocumentContext> contextSupplier;

    JsonPathAssertions(MemoizedSupplier<DocumentContext> contextSupplier) {
      this.contextSupplier = contextSupplier;
    }

    private static String resolveActualTypeName(@Nullable Object val) {
      if (val == null) {
        return "null";
      } else if (val instanceof Iterable<?> || val.getClass().isArray()) {
        return "array";
      } else if (val instanceof CharSequence) {
        return "string";
      } else if (val instanceof Map<?, ?>) {
        return "object";
      } else if (val instanceof BigDecimal || val instanceof Double) {
        return "decimal";
      } else if (val instanceof Long) {
        return "long";
      } else if (val instanceof Boolean) {
        return "boolean";
      } else if (val instanceof Integer) {
        return "integer";
      } else {
        return "unknown";
      }
    }

    @Nullable Object readVal(@Nullable @Language("jsonpath") String jsonPath) {
      if (jsonPath == null || jsonPath.isBlank()) {
        throw new IllegalArgumentException("jsonPath should be non-null and non-blank");
      }
      return contextSupplier.get().read(jsonPath, Object.class);
    }

    JsonPathAssertions stringPath(
        @Language("jsonpath") String jsonPath, ThrowingConsumer<JsonStringAssertions> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonStringAssertions(
                    jsonPath,
                    new MemoizedSupplier<>(
                        () -> {
                          var val = readVal(jsonPath);
                          if (!(val instanceof CharSequence)) {
                            throw new AssertionError(
                                String.format(
                                    "Expected value of type string at path \"%s\", but actual type was \"%s\"%s",
                                    jsonPath,
                                    resolveActualTypeName(val),
                                    val == null ? "" : " (" + val + ")"));
                          }
                          return ((CharSequence) val).toString();
                        })));
            return this;
          });
    }

    JsonPathAssertions intPath(
        @Language("jsonpath") String jsonPath,
        ThrowingConsumer<JsonNumberAssertions<Integer>> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonNumberAssertions<>(
                    jsonPath,
                    "Int number",
                    0,
                    new MemoizedSupplier<>(
                        () -> {
                          var val = contextSupplier.get().read(jsonPath, Object.class);
                          if (val instanceof Integer) {
                            return (Integer) val;
                          }
                          throw new AssertionError(
                              String.format(
                                  "Expected type Integer at path \"%s\", but actual type was \"%s\"%s",
                                  jsonPath,
                                  resolveActualTypeName(val),
                                  val == null ? "" : String.format(": <%s>", val)));
                        })));
            return this;
          });
    }

    JsonPathAssertions longPath(
        @Language("jsonpath") String jsonPath,
        ThrowingConsumer<JsonNumberAssertions<Long>> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonNumberAssertions<>(
                    jsonPath,
                    "Long number",
                    0L,
                    new MemoizedSupplier<>(
                        () -> {
                          var val = contextSupplier.get().read(jsonPath, Object.class);
                          if (val instanceof Long) {
                            return (Long) val;
                          } else if (val instanceof Integer) {
                            return ((Integer) val).longValue();
                          }
                          throw new AssertionError(
                              String.format(
                                  "Expected type Long at path \"%s\", but actual type was \"%s\"%s",
                                  jsonPath,
                                  resolveActualTypeName(val),
                                  val == null ? "" : String.format(": <%s>", val)));
                        })));
            return this;
          });
    }

    JsonPathAssertions decimalPath(
        @Language("jsonpath") String jsonPath,
        ThrowingConsumer<JsonNumberAssertions<BigDecimal>> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonNumberAssertions<>(
                    jsonPath,
                    "Decimal number",
                    BigDecimal.ZERO,
                    new MemoizedSupplier<>(
                        () -> {
                          var val = contextSupplier.get().read(jsonPath, Object.class);
                          if (val instanceof Double) {
                            return BigDecimal.valueOf((Double) val);
                          } else if (val instanceof BigDecimal) {
                            return (BigDecimal) val;
                          }
                          throw new AssertionError(
                              String.format(
                                  "Expected type Decimal at path \"%s\", but actual type was \"%s\"%s",
                                  jsonPath,
                                  resolveActualTypeName(val),
                                  val == null ? "" : String.format(": <%s>", val)));
                        })));
            return this;
          });
    }

    JsonPathAssertions stringArrayPath(
        @Language("jsonpath") String jsonPath,
        ThrowingConsumer<JsonStringArrayAssertions> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonStringArrayAssertions(
                    jsonPath,
                    new MemoizedSupplier<>(
                        () -> {
                          var val = contextSupplier.get().read(jsonPath, Object.class);
                          if (val instanceof Iterable<?>) {
                            var items = (Iterable<?>) val;
                            var objects = new ArrayList<String>();
                            for (var item : items) {
                              if (item instanceof String) {
                                objects.add((String) item);
                              } else {
                                throw new AssertionError(
                                    String.format(
                                        "Expected string array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, item.getClass().getName()));
                              }
                            }
                            return objects;
                          }
                          throw new AssertionError(
                              String.format(
                                  "Expected string array type at path \"%s\", but actual type was \"%s\"",
                                  jsonPath, resolveActualTypeName(val)));
                        })));
            return this;
          });
    }

    JsonPathAssertions intArrayPath(
        @Language("jsonpath") String jsonPath,
        ThrowingConsumer<JsonNumberArrayAssertions<Integer>> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonNumberArrayAssertions<>(
                    jsonPath,
                    "Int",
                    new MemoizedSupplier<>(
                        () -> {
                          var val = contextSupplier.get().read(jsonPath, Object.class);
                          if (val instanceof Iterable<?>) {
                            var items = (Iterable<?>) val;
                            var objects = new ArrayList<Integer>();
                            for (var item : items) {
                              if (item instanceof Integer) {
                                objects.add((Integer) item);
                              } else {
                                throw new AssertionError(
                                    String.format(
                                        "Expected int array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, resolveActualTypeName(item)));
                              }
                            }
                            return objects;
                          }
                          throw new AssertionError(
                              String.format(
                                  "Expected int array type at path \"%s\", but actual type was \"%s\"",
                                  jsonPath, val.getClass().getName()));
                        })));
            return this;
          });
    }

    JsonPathAssertions longArrayPath(
        @Language("jsonpath") String jsonPath,
        ThrowingConsumer<JsonNumberArrayAssertions<Long>> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonNumberArrayAssertions<>(
                    jsonPath,
                    "Long",
                    new MemoizedSupplier<>(
                        () -> {
                          var val = readVal(jsonPath);
                          if (val instanceof Iterable<?>) {
                            var items = (Iterable<?>) val;
                            var objects = new ArrayList<Long>();
                            for (var item : items) {
                              if (item instanceof Long) {
                                objects.add((Long) item);
                              } else if (item instanceof Integer) {
                                objects.add(((Integer) item).longValue());
                              } else {
                                throw new AssertionError(
                                    String.format(
                                        "Expected long array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, item.getClass().getName()));
                              }
                            }
                            return objects;
                          }
                          throw new AssertionError(
                              String.format(
                                  "Expected long array type at path \"%s\", but actual type was \"%s\"",
                                  jsonPath, val.getClass().getName()));
                        })));
            return this;
          });
    }

    JsonPathAssertions decimalArrayPath(
        @Language("jsonpath") String jsonPath,
        ThrowingConsumer<JsonNumberArrayAssertions<BigDecimal>> consumer) {
      return InternalUtils.sneakyGet(
          () -> {
            consumer.accept(
                new JsonNumberArrayAssertions<>(
                    jsonPath,
                    "Decimal",
                    new MemoizedSupplier<>(
                        () -> {
                          var val = contextSupplier.get().read(jsonPath, Object.class);
                          if (val instanceof Iterable<?>) {
                            var items = (Iterable<?>) val;
                            var objects = new ArrayList<BigDecimal>();
                            for (var item : items) {
                              if (item instanceof BigDecimal) {
                                objects.add((BigDecimal) item);
                              } else if (item instanceof Double) {
                                objects.add(BigDecimal.valueOf((Double) item));
                              } else {
                                throw new AssertionError(
                                    String.format(
                                        "Expected decimal array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, item.getClass().getName()));
                              }
                            }
                            return objects;
                          }
                          throw new AssertionError(
                              String.format(
                                  "Expected decimal array type at path \"%s\", but actual type was \"%s\"",
                                  jsonPath, val.getClass().getName()));
                        })));
            return this;
          });
    }

    JsonPathAssertions doesNotExist(@Language("jsonpath") String jsonPath) {
      try {
        var val = readVal(jsonPath);
        throw new AssertionError(
            String.format(
                "Expected value at path \"%s\" to be absent, but found <%s>", jsonPath, val));
      } catch (PathNotFoundException ignored) {
        return this;
      }
    }

    JsonPathAssertions isTrue(@Language("jsonpath") String jsonPath) {
      var val = contextSupplier.get().read(jsonPath, Object.class);
      if (val instanceof Boolean && (Boolean) val) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected value at path \"%s\" to be true, but actual value was %s", jsonPath, val));
    }

    JsonPathAssertions isFalse(@Language("jsonpath") String jsonPath) {
      var val = contextSupplier.get().read(jsonPath, Object.class);
      if (val instanceof Boolean && !((Boolean) val)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected value at path \"%s\" to be false, but actual value was %s", jsonPath, val));
    }

    JsonPathAssertions isNull(@Language("jsonpath") String jsonPath) {
      var val = readVal(jsonPath);
      if (val == null) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected value at path \"%s\" to be null, but actual value was <%s>",
              jsonPath, val));
    }

    JsonPathAssertions isNotNull(@Language("jsonpath") String jsonPath) {
      if (readVal(jsonPath) != null) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected value at path \"%s\" to be non-null, but actual value was null", jsonPath));
    }

    JsonPathAssertions isEqual(@Language("jsonpath") String jsonPath, CharSequence expected) {
      if (expected == null) {
        throw new IllegalArgumentException(
            "\"null\" expected values are not supported. Consider using `JsonPathAssertions#isNull()` instead");
      }
      var actual = readVal(jsonPath);
      if (actual instanceof CharSequence) {
        var expectedStr = expected.toString();
        if (actual.equals(expectedStr)) {
          return this;
        } else {
          throw new AssertionError(
              String.format(
                  "String value at path \"%s\" are not equal: %s",
                  jsonPath, InternalUtils.formatActualExpected(actual, expectedStr)));
        }
      }
      throw new AssertionError(
          String.format(
              "Expected value of type string at path \"%s\", but actual type was \"%s\"%s",
              jsonPath, resolveActualTypeName(actual), actual == null ? "" : " (" + actual + ")"));
    }
  }

  final class JsonNumberAssertions<N extends Number & Comparable<N>> {

    private final String path;
    private final String typeName;
    private final MemoizedSupplier<N> numberSupplier;
    private final N zero;

    JsonNumberAssertions(String path, String typeName, N zero, MemoizedSupplier<N> numberSupplier) {
      this.path = path;
      this.typeName = typeName;
      this.zero = zero;
      this.numberSupplier = numberSupplier;
    }

    public JsonNumberAssertions<N> isPositive() {
      var actualNum = numberSupplier.get();
      if (actualNum.compareTo(zero) > 0) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to be positive, but actual value was <%s>",
              typeName, path, actualNum));
    }

    public JsonNumberAssertions<N> isNegative() {
      var actualNum = numberSupplier.get();
      if (actualNum.compareTo(zero) < 0) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to be negative, but actual value was <%s>",
              typeName, path, actualNum.equals(zero) ? zero : actualNum));
    }

    public JsonNumberAssertions<N> isZero() {
      var actualNum = numberSupplier.get();
      if (actualNum.compareTo(zero) == 0) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to be zero, but actual value was <%s>",
              typeName, path, actualNum));
    }

    public JsonNumberAssertions<N> isEqualTo(N expected) {
      InternalUtils.expectedNotNull(expected);
      var actualNum = numberSupplier.get();
      if (actualNum.equals(expected)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to be equal <%s>, but actual value was <%s>",
              typeName, path, expected, actualNum));
    }

    public JsonNumberAssertions<N> isNotEqualTo(N expected) {
      InternalUtils.expectedNotNull(expected);
      var actualNum = numberSupplier.get();
      if (!actualNum.equals(expected)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to not be equal <%s>, but were equal",
              typeName, path, expected));
    }

    public JsonNumberAssertions<N> isGte(N expected) {
      InternalUtils.expectedNotNull(expected);
      var actualNum = numberSupplier.get();
      if (actualNum.compareTo(expected) >= 0) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to be greater than or equal to <%s>, but was <%s>",
              typeName, path, expected, actualNum));
    }

    public JsonNumberAssertions<N> isLte(N expected) {
      InternalUtils.expectedNotNull(expected);
      var actualNum = numberSupplier.get();
      if (actualNum.compareTo(expected) <= 0) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to be less than or equal to <%s>, but was <%s>",
              typeName, path, expected, actualNum));
    }

    public JsonNumberAssertions<N> isInRange(N min, N max) {
      InternalUtils.expectedNotNull(min, "Min");
      InternalUtils.expectedNotNull(max, "Max");
      if (min.compareTo(max) > 0) {
        throw new IllegalArgumentException(
            String.format(
                "Min value should be less than or equal to max value, but received min <%s> and max <%s>",
                min, max));
      }
      var actualNum = numberSupplier.get();
      if (actualNum.compareTo(min) >= 0 && actualNum.compareTo(max) <= 0) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected %s at path \"%s\" to be in range [%s - %s], but was <%s>",
              typeName, path, min, max, actualNum));
    }

    public JsonNumberAssertions<N> isIn(Iterable<N> expected) {
      InternalUtils.expectedNotNull(expected);
      var actualNum = numberSupplier.get();
      var expectedNums = InternalUtils.listFromIterable(expected);
      for (var expectedNum : expectedNums) {
        if (expectedNum.equals(actualNum)) {
          return this;
        }
      }
      throw new AssertionError(
          String.format(
              "%s at path \"%s\" is not in the list of expected values. Actual value: <%s>, list of expected values: %s",
              typeName, path, actualNum, expectedNums));
    }

    public JsonNumberAssertions<N> isNotIn(Iterable<N> expected) {
      InternalUtils.expectedNotNull(expected);
      var actualNum = numberSupplier.get();
      var expectedNums = InternalUtils.listFromIterable(expected);
      var counter = 0;
      for (var expectedNum : expectedNums) {
        if (expectedNum.equals(actualNum)) {
          throw new AssertionError(
              String.format(
                  "%s value at path \"%s\" was found in provided list at index [%d]. Actual value: <%s>, list of values: <%s>",
                  typeName, path, counter, actualNum, expectedNums));
        }
        counter++;
      }
      return this;
    }

    public JsonNumberAssertions<N> satisfies(ThrowingConsumer<N> consumer) {
      InternalUtils.expectedNotNull(consumer, "Consumer");
      return InternalUtils.satisfies(consumer, numberSupplier, this, typeName, path);
    }
  }

  final class JsonStringAssertions {

    private final String path;

    private final MemoizedSupplier<String> stringSupplier;

    JsonStringAssertions(String path, MemoizedSupplier<String> stringSupplier) {
      this.path = path;
      this.stringSupplier = stringSupplier;
    }

    public JsonStringAssertions isEqualTo(CharSequence expected) {
      var actual = stringSupplier.get();
      var expectedStr = expected.toString();
      if (actual.equals(expectedStr)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" is not equal to expected: %s",
              path, InternalUtils.formatActualExpected(actual, expectedStr)));
    }

    public JsonStringAssertions isNotEqualTo(CharSequence expected) {
      if (!stringSupplier.get().equals(expected.toString())) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" is equal to <%s>, while expected to be not equal",
              path, expected));
    }

    public JsonStringAssertions isEqualToIgnoringCase(CharSequence expected) {
      var actualString = stringSupplier.get();
      var expectedString = expected.toString();
      if (actualString.equalsIgnoreCase(expectedString)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" is not equal to expected (ignoring case): %s",
              path, InternalUtils.formatActualExpected(actualString, expectedString)));
    }

    public JsonStringAssertions isNotEqualToIgnoringCase(CharSequence expected) {
      var actual = stringSupplier.get();
      var expectedStr = expected.toString();
      if (!actual.equalsIgnoreCase(expectedStr)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" is equal to <%s> (ignoring case), while expected to be not equal",
              path, expected));
    }

    public JsonStringAssertions isNotBlank() {
      var subject = stringSupplier.get();
      if (subject.isBlank()) {
        throw new AssertionError(
            String.format(
                "Expected string at path \"%s\" to be not blank, but actual value was \"%s\"",
                path, subject));
      }
      return this;
    }

    public JsonStringAssertions isBlank() {
      var subject = stringSupplier.get();
      if (!subject.isBlank()) {
        throw new AssertionError(
            String.format(
                "Expected string at path \"%s\" to be blank, but actual value was \"%s\"",
                path, subject));
      }
      return this;
    }

    public JsonStringAssertions isEmpty() {
      var subject = stringSupplier.get();
      if (subject.isEmpty()) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected string at path \"%s\" to be empty, but actual value was <%s>",
              path, subject));
    }

    public JsonStringAssertions isNotEmpty() {
      var subject = stringSupplier.get();
      if (!subject.isEmpty()) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected string at path \"%s\" to be not empty, but actual value was empty", path));
    }

    public JsonStringAssertions hasLength(int length) {
      if (length < 0) {
        throw new IllegalArgumentException(
            String.format("Length cannot be negative (received %d)", length));
      }
      var actual = stringSupplier.get().length();
      if (actual == length) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected string at path \"%s\" to have length [%d], but actual length was [%d]",
              path, length, actual));
    }

    public JsonStringAssertions hasLengthRange(int lengthMin, int lengthMax) {
      if (lengthMin < 0) {
        throw new IllegalArgumentException(
            String.format("Min length cannot be negative (received %d)", lengthMin));
      } else if (lengthMax < 0) {
        throw new IllegalArgumentException(
            String.format("Max length cannot be negative (received %d)", lengthMax));
      } else if (lengthMin > lengthMax) {
        throw new IllegalArgumentException(
            String.format(
                "Min length cannot be greater than max length (received %d > %d)",
                lengthMin, lengthMax));
      }
      var actual = stringSupplier.get();
      if (actual.length() >= lengthMin && actual.length() <= lengthMax) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected string at path \"%s\" to be in range [%d - %d], but actual length was [%d]",
              path, lengthMin, lengthMax, actual.length()));
    }

    public JsonStringAssertions hasLengthAtLeast(int lengthMin) {
      if (lengthMin < 0) {
        throw new IllegalArgumentException(
            String.format("Min length cannot be negative (received %d)", lengthMin));
      }
      var actualLength = stringSupplier.get().length();
      if (actualLength >= lengthMin) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected string at path \"%s\" to have min length [%d], but actual length was [%d]",
              path, actualLength, lengthMin));
    }

    public JsonStringAssertions hasLengthAtMost(int lengthMax) {
      if (lengthMax < 0) {
        throw new IllegalArgumentException(
            String.format("Max length cannot be negative (received %d)", lengthMax));
      }
      var actualLength = stringSupplier.get().length();
      if (actualLength <= lengthMax) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "Expected string at path \"%s\" to have max length [%d], but actual length was [%d]",
              path, actualLength, lengthMax));
    }

    public JsonStringAssertions contains(CharSequence expected) {
      var actualStr = stringSupplier.get();
      var expectedStr = expected.toString();
      if (actualStr.contains(expectedStr)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" does not contain expected string: %s",
              path, InternalUtils.formatActualExpected(actualStr, expected)));
    }

    public JsonStringAssertions containsIgnoringCase(CharSequence expected) {
      var actualStr = stringSupplier.get();
      var expectedStr = expected.toString();
      var expectedLower = expectedStr.toLowerCase();
      if (actualStr.toLowerCase().contains(expectedLower)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" does not contain expected string (ignoring case): %s",
              path, InternalUtils.formatActualExpected(actualStr, expectedStr)));
    }

    public JsonStringAssertions matches(@Language("regexp") String pattern) {
      var actualStr = stringSupplier.get();
      if (actualStr.matches(pattern)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" does not match expected pattern. Expected pattern: <%s>, actual value: <%s>",
              path, pattern, actualStr));
    }

    public JsonStringAssertions doesNotMatch(@Language("regexp") String pattern) {
      var actualStr = stringSupplier.get();
      if (!actualStr.matches(pattern)) {
        return this;
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" matches expected pattern, while expected to not match. Pattern: <%s>, actual value: <%s>",
              path, pattern, actualStr));
    }

    public JsonStringAssertions isIn(Iterable<? extends CharSequence> expected) {
      var actualStr = stringSupplier.get();
      var expectedStrings = InternalUtils.listFromIterable(expected, CharSequence::toString);
      for (var expectedStr : expectedStrings) {
        if (expectedStr.equals(actualStr)) {
          return this;
        }
      }
      throw new AssertionError(
          String.format(
              "String value at path \"%s\" is not in the list of expected values. Actual value: <%s>, list of expected values: <%s>",
              path, actualStr, expectedStrings));
    }

    public JsonStringAssertions isNotIn(Iterable<? extends CharSequence> expected) {
      var actualStr = stringSupplier.get();
      var expectedStrings = InternalUtils.listFromIterable(expected, CharSequence::toString);
      var counter = 0;
      for (var expectedStr : expectedStrings) {
        if (expectedStr.equals(actualStr)) {
          throw new AssertionError(
              String.format(
                  "String value at path \"%s\" was found in provided list at index [%d]. Actual value: <%s>, list of values: <%s>",
                  path, counter, actualStr, expectedStrings));
        }
        counter++;
      }
      return this;
    }

    public JsonStringAssertions satisfies(ThrowingConsumer<String> consumer) {
      InternalUtils.expectedNotNull(consumer, "Consumer");
      return InternalUtils.satisfies(consumer, stringSupplier, this, "String", path);
    }
  }

  final class JsonNumberArrayAssertions<N extends Number & Comparable<N>> {

    private final String path;
    private final String arrayType;
    private final MemoizedSupplier<List<N>> numbersSupplier;

    JsonNumberArrayAssertions(
        String path, String arrayType, MemoizedSupplier<List<N>> numbersSupplier) {
      this.path = path;
      this.arrayType = arrayType;
      this.numbersSupplier = numbersSupplier;
    }

    public JsonNumberArrayAssertions<N> hasSize(int size) {
      return InternalUtils.hasSize(this, numbersSupplier, size, path, arrayType);
    }

    public JsonNumberArrayAssertions<N> containsAll(Iterable<? extends N> expected) {
      return InternalUtils.containsAll(
          this,
          numbersSupplier,
          StreamSupport.stream(Spliterators.spliteratorUnknownSize(expected.iterator(), 0), false),
          path,
          "Number");
    }

    public JsonNumberArrayAssertions<N> containsAny(Iterable<? extends N> expected) {
      return InternalUtils.containsAny(
          this,
          numbersSupplier,
          StreamSupport.stream(Spliterators.spliteratorUnknownSize(expected.iterator(), 0), false),
          path,
          "Number");
    }

    public JsonNumberArrayAssertions<N> allSatisfy(ThrowingConsumer<N> consumer) {
      return InternalUtils.allSatisfy(this, numbersSupplier, consumer);
    }

    public JsonNumberArrayAssertions<N> anySatisfy(ThrowingConsumer<N> consumer) {
      return InternalUtils.anySatisfy(this, numbersSupplier, consumer);
    }

    public JsonNumberArrayAssertions<N> satisfy(ThrowingConsumer<List<N>> consumer) {
      return InternalUtils.satisfy(this, numbersSupplier, consumer);
    }
  }

  final class JsonStringArrayAssertions {

    private final String path;
    private final MemoizedSupplier<List<String>> stringsSupplier;

    JsonStringArrayAssertions(String path, MemoizedSupplier<List<String>> stringsSupplier) {
      this.path = path;
      this.stringsSupplier = stringsSupplier;
    }

    public JsonStringArrayAssertions hasSize(int size) {
      return InternalUtils.hasSize(this, stringsSupplier, size, path, "String");
    }

    public JsonStringArrayAssertions containsAll(Iterable<? extends CharSequence> expected) {
      return InternalUtils.containsAll(
          this,
          stringsSupplier,
          StreamSupport.stream(Spliterators.spliteratorUnknownSize(expected.iterator(), 0), false)
              .map(CharSequence::toString),
          path,
          "String");
    }

    public JsonStringArrayAssertions containsAny(Iterable<? extends CharSequence> expected) {
      return InternalUtils.containsAny(
          this,
          stringsSupplier,
          StreamSupport.stream(Spliterators.spliteratorUnknownSize(expected.iterator(), 0), false)
              .map(CharSequence::toString),
          path,
          "String");
    }

    public JsonStringArrayAssertions allSatisfy(ThrowingConsumer<String> consumer) {
      return InternalUtils.allSatisfy(this, stringsSupplier, consumer);
    }

    public JsonStringArrayAssertions anySatisfy(ThrowingConsumer<String> consumer) {
      return InternalUtils.anySatisfy(this, stringsSupplier, consumer);
    }

    public JsonStringArrayAssertions satisfy(ThrowingConsumer<List<String>> consumer) {
      return InternalUtils.satisfy(this, stringsSupplier, consumer);
    }
  }

  interface ThrowingConsumer<T> {

    void accept(T t) throws Throwable;

    default Consumer<T> toUnchecked() {
      return t -> {
        try {
          accept(t);
        } catch (Throwable e) {
          InternalUtils.rethrow(e);
        }
      };
    }
  }

  interface ThrowingSupplier<T> {

    T get() throws Throwable;
  }
}

final class MemoizedSupplier<T> implements Supplier<T> {

  private final Supplier<T> supplier;
  @Nullable private T value;

  MemoizedSupplier(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    @Nullable var val = value;
    if (val != null) {
      return val;
    }
    val = supplier.get();
    value = val;
    return val;
  }
}

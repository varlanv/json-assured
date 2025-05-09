package com.varlanv.jsonassured;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface JsonAssured {

    static JsonPathAssertions assertJson(byte[] bytes) {
        return assertJson(new ByteArrayInputStream(bytes));
    }

    static JsonPathAssertions assertJson(InputStream is) {
        Objects.requireNonNull(is);
        return new JsonPathAssertions(new MemoizedSupplier<>(() -> JsonPath.parse(is)));
    }

    static JsonPathAssertions assertJson(Reader reader) {
        var str = new BufferedReader(reader).lines().collect(Collectors.joining(System.lineSeparator()));
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
            } else if (val instanceof Iterable<?>) {
                return "array";
            } else if (val instanceof CharSequence) {
                return "string";
            } else if (val instanceof Map<?, ?>) {
                return "object";
            } else {
                return "number";
            }
        }

        @Nullable
        Object readVal(@Language("jsonpath") String jsonPath) {
            return contextSupplier.get().read(jsonPath, Object.class);
        }

        JsonPathAssertions stringPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonStringAssertions> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonStringAssertions(jsonPath, new MemoizedSupplier<>(() -> {
                    var val = readVal(jsonPath);
                    if (!(val instanceof String)) {
                        throw new AssertionError(String.format("Expected value of type string at path \"%s\", but actual type was \"%s\"%s",
                                jsonPath, resolveActualTypeName(val), val == null ? "" : " (" + val + ")"));
                    }
                    return (String) val;
                })));
                return this;
            });
        }

        JsonPathAssertions intPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonNumberAssertions<Integer>> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonNumberAssertions<>(jsonPath, 0, new MemoizedSupplier<>(() -> {
                    var val = contextSupplier.get().read(jsonPath, Object.class);
                    if (val instanceof Integer) {
                        return (Integer) val;
                    }
                    throw new AssertionError(String.format("Expected type Integer at path \"%s\", but actual type was \"%s\"", jsonPath, val.getClass().getName()));
                })));
                return this;
            });
        }

        JsonPathAssertions longPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonNumberAssertions<Long>> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonNumberAssertions<>(jsonPath, 0L, new MemoizedSupplier<>(() -> {
                    var val = contextSupplier.get().read(jsonPath, Object.class);
                    if (val instanceof Long) {
                        return (Long) val;
                    } else if (val instanceof Integer) {
                        return ((Integer) val).longValue();
                    }
                    throw new AssertionError(String.format("Expected type Long at path \"%s\", but actual type was \"%s\"", jsonPath, val.getClass().getName()));
                })));
                return this;
            });
        }

        JsonPathAssertions decimalPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonNumberAssertions<BigDecimal>> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonNumberAssertions<>(jsonPath, BigDecimal.ZERO, new MemoizedSupplier<>(() -> {
                    var val = contextSupplier.get().read(jsonPath, Object.class);
                    if (val instanceof Double) {
                        return BigDecimal.valueOf((Double) val);
                    } else if (val instanceof BigDecimal) {
                        return (BigDecimal) val;
                    }
                    throw new AssertionError(String.format("Expected type BigDecimal at path \"%s\", but actual type was \"%s\"", jsonPath, val.getClass().getName()));
                })));
                return this;
            });
        }

        JsonPathAssertions stringArrayPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonStringArrayAssertions> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonStringArrayAssertions(jsonPath, new MemoizedSupplier<>(() -> {
                    var val = contextSupplier.get().read(jsonPath, Object.class);
                    if (val instanceof Iterable<?>) {
                        var items = (Iterable<?>) val;
                        var objects = new ArrayList<String>();
                        for (var item : items) {
                            if (item instanceof String) {
                                objects.add((String) item);
                            } else {
                                throw new AssertionError(String.format("Expected string array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, item.getClass().getName()));
                            }
                        }
                        return objects;
                    }
                    throw new AssertionError(String.format("Expected string array type at path \"%s\", but actual type was \"%s\"", jsonPath, val.getClass().getName()));
                })));
                return this;
            });
        }

        JsonPathAssertions intArrayPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonNumberArrayAssertions<Integer>> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonNumberArrayAssertions<>(jsonPath, "Int", new MemoizedSupplier<>(() -> {
                    var val = contextSupplier.get().read(jsonPath, Object.class);
                    if (val instanceof Iterable<?>) {
                        var items = (Iterable<?>) val;
                        var objects = new ArrayList<Integer>();
                        for (var item : items) {
                            if (item instanceof Integer) {
                                objects.add((Integer) item);
                            } else {
                                throw new AssertionError(String.format("Expected int array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, item.getClass().getName()));
                            }
                        }
                        return objects;
                    }
                    throw new AssertionError(String.format("Expected int array type at path \"%s\", but actual type was \"%s\"", jsonPath, val.getClass().getName()));
                })));
                return this;
            });
        }

        JsonPathAssertions longArrayPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonNumberArrayAssertions<Long>> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonNumberArrayAssertions<>(jsonPath, "Long", new MemoizedSupplier<>(() -> {
                    var val = contextSupplier.get().read(jsonPath, Object.class);
                    if (val instanceof Iterable<?>) {
                        var items = (Iterable<?>) val;
                        var objects = new ArrayList<Long>();
                        for (var item : items) {
                            if (item instanceof Long) {
                                objects.add((Long) item);
                            } else if (item instanceof Integer) {
                                objects.add(((Integer) item).longValue());
                            } else {
                                throw new AssertionError(String.format("Expected long array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, item.getClass().getName()));
                            }
                        }
                        return objects;
                    }
                    throw new AssertionError(String.format("Expected long array type at path \"%s\", but actual type was \"%s\"", jsonPath, val.getClass().getName()));
                })));
                return this;
            });
        }

        JsonPathAssertions decimalArrayPath(@Language("jsonpath") String jsonPath, ThrowingConsumer<JsonNumberArrayAssertions<BigDecimal>> consumer) {
            return InternalUtils.sneakyGet(() -> {
                consumer.accept(new JsonNumberArrayAssertions<>(jsonPath, "Decimal", new MemoizedSupplier<>(() -> {
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
                                throw new AssertionError(String.format("Expected decimal array type at path \"%s\", but actual type of value in array was \"%s\"",
                                        jsonPath, item.getClass().getName()));
                            }
                        }
                        return objects;
                    }
                    throw new AssertionError(String.format("Expected decimal array type at path \"%s\", but actual type was \"%s\"", jsonPath, val.getClass().getName()));
                })));
                return this;
            });
        }

        JsonPathAssertions doesNotExist(@Language("jsonpath") String jsonPath) {
            try {
                var val = readVal(jsonPath);
                throw new AssertionError(String.format("Expected value at path \"%s\" to be absent, but found <%s>", jsonPath, val));
            } catch (PathNotFoundException e) {
                return this;
            }
        }

        JsonPathAssertions isTrue(@Language("jsonpath") String jsonPath) {
            var val = contextSupplier.get().read(jsonPath, Object.class);
            if (val instanceof Boolean && (Boolean) val) {
                return this;
            }
            throw new AssertionError(String.format("Expected value at path \"%s\" to be true, but actual value was %s", jsonPath, val));
        }

        JsonPathAssertions isFalse(@Language("jsonpath") String jsonPath) {
            var val = contextSupplier.get().read(jsonPath, Object.class);
            if (val instanceof Boolean && !((Boolean) val)) {
                return this;
            }
            throw new AssertionError(String.format("Expected value at path \"%s\" to be false, but actual value was %s",
                    jsonPath, val));
        }

        JsonPathAssertions isNull(@Language("jsonpath") String jsonPath) {
            var val = contextSupplier.get().read(jsonPath, Object.class);
            if (val == null) {
                return this;
            }
            throw new AssertionError(String.format("Expected value at path \"%s\" to be null, but actual value was %s", jsonPath, val));
        }

        JsonPathAssertions isNotNull(@Language("jsonpath") String jsonPath) {
            var val = contextSupplier.get().read(jsonPath, Object.class);
            if (val != null) {
                return this;
            }
            throw new AssertionError(String.format("Expected value at path \"%s\" to be non-null, but actual value was null", jsonPath));
        }
    }

    final class JsonNumberAssertions<N extends Number & Comparable<N>> {

        private final String path;
        private final MemoizedSupplier<N> numberSupplier;
        private final N zero;

        JsonNumberAssertions(String path, N zero, MemoizedSupplier<N> numberSupplier) {
            this.path = path;
            this.zero = zero;
            this.numberSupplier = numberSupplier;
        }

        public JsonNumberAssertions<N> isPositive() {
            if (numberSupplier.get().compareTo(zero) > 0) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" is not positive", path));
        }

        public JsonNumberAssertions<N> isNegative() {
            if (numberSupplier.get().compareTo(zero) < 0) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" is not negative", path));
        }

        public JsonNumberAssertions<N> isZero() {
            if (numberSupplier.get().compareTo(zero) == 0) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" is not zero", path));
        }

        public JsonNumberAssertions<N> isEqualTo(N expected) {
            if (numberSupplier.get().equals(expected)) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" does not equal \"%s\"", path, expected));
        }

        public JsonNumberAssertions<N> isNotEqualTo(N expected) {
            if (!numberSupplier.get().equals(expected)) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" equal \"%s\"", path, expected));
        }

        public JsonNumberAssertions<N> isGte(N expected) {
            if (numberSupplier.get().compareTo(expected) >= 0) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" is not greater than or equal to \"%s\"", path, expected));
        }

        public JsonNumberAssertions<N> isLte(N expected) {
            if (numberSupplier.get().compareTo(expected) <= 0) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" is not less than or equal to \"%s\"", path, expected));
        }

        public JsonNumberAssertions<N> isInRange(N min, N max) {
            if (numberSupplier.get().compareTo(min) >= 0 && numberSupplier.get().compareTo(max) <= 0) {
                return this;
            }
            throw new AssertionError(String.format("Number at path \"%s\" is not in range [%s, %s]", path, min, max));
        }

        public JsonNumberAssertions<N> isIn(Iterable<N> expected) {
            return InternalUtils.isIn(numberSupplier.get(), expected, this);
        }

        public JsonNumberAssertions<N> isNotIn(Iterable<N> expected) {
            var val = numberSupplier.get();
            for (var n : expected) {
                if (n.equals(val)) {
                    throw new AssertionError("Is in");
                }
            }
            return this;
        }

        public JsonNumberAssertions<N> satisfies(ThrowingConsumer<N> consumer) {
            consumer.toUnchecked().accept(numberSupplier.get());
            return this;
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
            throw new AssertionError(String.format("String value at path \"%s\" are not equal:%s", path, InternalUtils.formatActualExpected(actual, expectedStr)));
        }

        public JsonStringAssertions isNotEqualTo(CharSequence expected) {
            if (!stringSupplier.get().equals(expected.toString())) {
                return this;
            }
            throw new AssertionError(String.format("String value at path \"%s\" is equal to <%s>, while expected to be not equal", path, expected));
        }

        public JsonStringAssertions isEqualToIgnoringCase(CharSequence expected) {
            if (stringSupplier.get().equalsIgnoreCase(expected.toString())) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" does not equal \"%s\" (ignoring case)", path, expected));
        }

        public JsonStringAssertions isNotEqualToIgnoringCase(CharSequence expected) {
            if (!stringSupplier.get().equalsIgnoreCase(expected.toString())) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" equal \"%s\" (ignoring case)", path, expected));
        }

        public JsonStringAssertions isNotBlank() {
            var subject = stringSupplier.get();
            if (subject.isBlank()) {
                throw new AssertionError(String.format("Expected string at path \"%s\" to be not blank, but actual value was \"%s\"",
                        path, subject));
            }
            return this;
        }

        public JsonStringAssertions isBlank() {
            var subject = stringSupplier.get();
            if (!subject.isBlank()) {
                throw new AssertionError(String.format("Expected string at path \"%s\" to be blank, but actual value was \"%s\"",
                        path, subject));
            }
            return this;
        }

        public JsonStringAssertions isEmpty() {
            var subject = stringSupplier.get();
            if (!subject.isEmpty()) {
                return this;
            }
            throw new AssertionError(String.format("Expected string at path \"%s\" to be empty, but actual value was not empty", path));
        }

        public JsonStringAssertions isNotEmpty() {
            var subject = stringSupplier.get();
            if (!subject.isEmpty()) {
                return this;
            }
            throw new AssertionError(String.format("Expected string at path \"%s\" to be not empty, but actual value was empty", path));
        }

        public JsonStringAssertions hasLength(int length) {
            if (stringSupplier.get().length() == length) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" has length %d, but expected length is %d",
                    path, stringSupplier.get().length(), length));
        }

        public JsonStringAssertions hasLengthRange(int lengthMin, int lengthMax) {
            var actual = stringSupplier.get();
            if (actual.length() >= lengthMin && actual.length() <= lengthMax) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" has length %d, but expected length range is [%d, %d]",
                    path, actual.length(), lengthMin, lengthMax));
        }

        public JsonStringAssertions hasMinLength(int lengthMin) {
            if (stringSupplier.get().length() >= lengthMin) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" has length %d, but expected min length is %d",
                    path, stringSupplier.get().length(), lengthMin));
        }

        public JsonStringAssertions hasMaxLength(int lengthMax) {
            if (stringSupplier.get().length() <= lengthMax) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" has length %d, but expected max length is %d",
                    path, stringSupplier.get().length(), lengthMax));
        }

        public JsonStringAssertions contains(CharSequence expected) {
            var actual = stringSupplier.get();
            var expectedVal = expected.toString();
            if (actual.contains(expectedVal)) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" does not contain \"%s\"", path, expectedVal));
        }

        public JsonStringAssertions containsIgnoringCase(CharSequence expected) {
            var actual = stringSupplier.get();
            var expectedVal = expected.toString();
            var expectedLower = expectedVal.toLowerCase();
            if (actual.toLowerCase().contains(expectedLower)) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" does not contain \"%s\" (ignoring case)", path, expectedVal));
        }

        public JsonStringAssertions matches(@Language("regexp") String pattern) {
            if (stringSupplier.get().matches(pattern)) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" does not match pattern \"%s\"", path, pattern));
        }

        public JsonStringAssertions doesNotMatch(@Language("regexp") String pattern) {
            if (!stringSupplier.get().matches(pattern)) {
                return this;
            }
            throw new AssertionError(String.format("String at path \"%s\" matches pattern \"%s\"", path, pattern));
        }

        public JsonStringAssertions isIn(Iterable<? extends CharSequence> expected) {
            var actual = stringSupplier.get();
            for (var n : expected) {
                if (n.toString().equals(actual)) {
                    return this;
                }
            }
            throw new AssertionError("Not in");
        }

        public JsonStringAssertions isNotIn(Iterable<? extends CharSequence> expected) {
            var actual = stringSupplier.get();
            for (var n : expected) {
                if (n.toString().equals(actual)) {
                    throw new AssertionError("Not in");
                }
            }
            return this;
        }

        public JsonStringAssertions satisfies(ThrowingConsumer<String> consumer) {
            consumer.toUnchecked().accept(stringSupplier.get());
            return this;
        }
    }

    final class JsonNumberArrayAssertions<N extends Number & Comparable<N>> {

        private final String path;
        private final String arrayType;
        private final MemoizedSupplier<List<N>> numbersSupplier;

        JsonNumberArrayAssertions(String path, String arrayType, MemoizedSupplier<List<N>> numbersSupplier) {
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
                    "Number"
            );
        }

        public JsonNumberArrayAssertions<N> containsAny(Iterable<? extends N> expected) {
            return InternalUtils.containsAny(
                    this,
                    numbersSupplier,
                    StreamSupport.stream(Spliterators.spliteratorUnknownSize(expected.iterator(), 0), false),
                    path,
                    "Number"
            );
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
                    "String"
            );
        }

        public JsonStringArrayAssertions containsAny(Iterable<? extends CharSequence> expected) {
            return InternalUtils.containsAny(
                    this,
                    stringsSupplier,
                    StreamSupport.stream(Spliterators.spliteratorUnknownSize(expected.iterator(), 0), false)
                            .map(CharSequence::toString),
                    path,
                    "String"
            );
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
                    InternalUtils.sneakyThrow(e);
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
    @Nullable
    private T value;

    MemoizedSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        @Nullable
        var val = value;
        if (val != null) {
            return val;
        }
        val = supplier.get();
        value = val;
        return val;
    }
}

interface InternalUtils {

    static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        @SuppressWarnings("unchecked")
        var res = (T) t;
        throw res;
    }

    static <T> T sneakyGet(JsonAssured.ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return sneakyThrow(e);
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
        var expectedList = expected.toList();
        if (expectedList.isEmpty()) {
            throw new IllegalArgumentException("Array of expected values cannot be empty");
        }
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
        var expectedList = expected.toList();
        if (expectedList.isEmpty()) {
            throw new IllegalArgumentException("Array of expected values cannot be empty");
        }
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
        return String.format(" Expected: <%s> but was: <%s>", expected, actual);
    }
}

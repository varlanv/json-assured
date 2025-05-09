package com.varlanv.jsonassured;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

class JsonAssuredTest {

    @Language("json")
    private static final String jsonAllTypes = """
            {
            "stringVal": "sTr",
            "zeroIntVal": 0,
            "zeroDecimalVal": 0.0,
            "intAsStringVal": "1234",
            "decimalAsStringVal": "1234.5678",
            "positiveIntVal": 123456789,
            "negativeIntVal": -123456789,
            "positiveLongVal": 1234567890123456,
            "negativeLongVal": -1234567890123456,
            "positiveDecimalVal" : 123456789.123456789,
            "negativeDecimalVal" : -123456789.123456789,
            "booleanTrue": true,
            "booleanFalse": false,
            "nullVal": null,
            "objectVal": {
              "nestedStringVal": "str",
              "nestedIntVal": 123456789
            },
            "stringsArray": ["a","b","c"],
            "intArray": [1, 2, 3],
            "longArray": [1234567890123456],
            "decimalArray": [123456789.123456789, 123456789.223456789],
            "objectsArray": [
                {
                  "nestedIntVal": 1234567
                },
                {
                  "nestedIntVal": 12345678
                }
                ]
            }""";


    static Stream<JsonAssured.JsonPathAssertions> test_happy() {
        var bytes = jsonAllTypes.getBytes(StandardCharsets.UTF_8);
        return Stream.of(
                JsonAssured.assertJson(jsonAllTypes),
                JsonAssured.assertJson(bytes),
                JsonAssured.assertJson(new StringReader(jsonAllTypes)),
                JsonAssured.assertJson(new ByteArrayInputStream(bytes))
        );
    }

    @MethodSource
    @ParameterizedTest
    void test_happy(JsonAssured.JsonPathAssertions subject) {
        subject
                .isTrue("$.booleanTrue")
                .isFalse("$.booleanFalse")
                .isNull("$.nullVal")
                .isNotNull("$.objectVal").isNotNull("$.stringsArray").isNotNull("$.objectsArray").isNotNull("$.booleanFalse")
                .isNotNull("$.stringVal").isNotNull("$.zeroIntVal").isNotNull("$.decimalAsStringVal").isNotNull("$.booleanTrue")
                .stringPath("$.stringVal", strVal -> strVal
                        .isEqualTo("sTr").isNotEqualTo("str")
                        .isEqualToIgnoringCase("str").isNotEqualToIgnoringCase("st")
                        .contains("s").contains("r").contains("T").contains("sT")
                        .containsIgnoringCase("str").containsIgnoringCase("tr").containsIgnoringCase("st")
                        .hasLength(3)
                        .hasLengthRange(2, 4)
                        .hasMinLength(1).hasMinLength(2).hasMinLength(3)
                        .hasMaxLength(3).hasMaxLength(4).hasMaxLength(5)
                        .isIn(List.of("sTr")).isIn(List.of("sTr", "stTr", "sT"))
                        .isNotIn(List.of("str", "stTr", "sT"))
                        .matches("^[a-zA-Z]+$")
                        .matches("sTr")
                        .matches("s[a-zA-Z]+")
                        .doesNotMatch("^[0-9]+$")
                        .satisfies(it -> {
                        }))
                .intPath("$.zeroIntVal", intVal -> intVal
                        .isZero()
                        .isEqualTo(0).isNotEqualTo(1).isNotEqualTo(-1)
                        .isIn(List.of(0)).isIn(List.of(-1, 0, 1))
                        .isLte(0).isLte(1).isLte(123456789)
                        .isGte(0).isGte(-1).isGte(-123456789))
                .intPath("$.positiveIntVal", intVal -> intVal
                        .isEqualTo(123456789).isNotEqualTo(12345678)
                        .isPositive()
                        .isGte(-1).isGte(0).isGte(1).isGte(123456789)
                        .isLte(123456789).isLte(1234567890)
                        .isInRange(-1, 1234567890).isInRange(-1, 123456789)
                        .isNotIn(List.of(12345678, 1234567890))
                        .satisfies(it -> {
                        }))
                .decimalPath("$.zeroDecimalVal", decimalVal -> decimalVal
                        .isZero()
                        .isGte(BigDecimal.valueOf(-1))
                        .isGte(BigDecimal.valueOf(0))
                        .isLte(BigDecimal.valueOf(0))
                        .isGte(BigDecimal.valueOf(-123456789))
                        .isIn(List.of(BigDecimal.valueOf(-1), BigDecimal.valueOf(0.0), BigDecimal.valueOf(1)))
                        .isNotIn(List.of(BigDecimal.valueOf(-123456789), BigDecimal.valueOf(123456789), BigDecimal.ZERO, BigDecimal.valueOf(0)))
                        .satisfies(it -> {
                        }))
                .decimalPath("$.positiveDecimalVal", decimalVal -> decimalVal
                        .isEqualTo(new BigDecimal("123456789.123456789"))
                        .isNotEqualTo(new BigDecimal("123456789.123456788"))
                        .isGte(new BigDecimal("123456789.123456789")).isGte(BigDecimal.valueOf(-123456789.123456789)).isGte(BigDecimal.ZERO).isGte(BigDecimal.ONE)
                        .isLte(new BigDecimal("123456789.123456789")).isLte(new BigDecimal("123456789.223456789"))
                        .isPositive()
                        .isInRange(BigDecimal.valueOf(123456789), BigDecimal.valueOf(123456790))
                        .isIn(List.of(new BigDecimal("123456789.123456789")))
                        .isNotIn(List.of(BigDecimal.ZERO))
                        .satisfies(d -> {
                        }))
                .stringPath("$.intAsStringVal", strVal -> strVal
                        .isEqualTo("1234")
                        .matches("^[0-9]+$"))
                .stringPath("$.decimalAsStringVal", strVal -> strVal
                        .isEqualTo("1234.5678")
                        .matches("^[0-9]+(\\.[0-9]+)?$"))
                .intPath("$.negativeIntVal", intVal -> intVal
                        .isNegative().isEqualTo(-123456789)
                        .isLte(-123456789).isLte(-1).isLte(0))
                .intPath("$.objectsArray.size()", size -> size.isEqualTo(2))
                .intPath("$.objectsArray.length()", size -> size.isEqualTo(2))
                .intPath("$.stringsArray.size()", size -> size.isEqualTo(3))
                .intPath("$.stringsArray.length()", size -> size.isEqualTo(3))
                .intPath("$.objectVal.size()", size -> size.isEqualTo(2))
                .intPath("$.objectVal.length()", size -> size.isEqualTo(2))
                .intPath("$.objectVal.nestedIntVal", intVal -> intVal.isEqualTo(123456789))
                .stringPath("$.objectVal.nestedStringVal", strVal -> strVal.isEqualTo("str"))
                .intPath("$.objectsArray[0].nestedIntVal", intVal -> intVal.isEqualTo(1234567))
                .intPath("$.objectsArray[1].nestedIntVal", intVal -> intVal.isEqualTo(12345678))
                .stringPath("$.stringsArray[0]", strVal -> strVal.isEqualTo("a"))
                .stringPath("$.stringsArray[1]", strVal -> strVal.isEqualTo("b"))
                .stringPath("$.stringsArray[2]", strVal -> strVal.isEqualTo("c"))
                .stringArrayPath("$.stringsArray[*]", strVals -> strVals
                        .hasSize(3)
                        .anySatisfy(val -> Assertions.assertEquals("a", val))
                        .allSatisfy(val -> Assertions.assertEquals(1, val.length()))
                        .satisfy(vals -> Assertions.assertEquals(3, vals.size())))
                .intArrayPath("$.intArray[*]", intVals -> intVals
                        .hasSize(3)
                        .containsAll(List.of(1, 2, 3))
                        .containsAny(List.of(0, 2, 5))
                        .anySatisfy(val -> Assertions.assertEquals(1, val))
                        .allSatisfy(val -> Assertions.assertTrue(val < 10))
                        .satisfy(vals -> Assertions.assertEquals(3, vals.size())))
                .longArrayPath("$.longArray[*]", longVals -> longVals
                        .hasSize(1)
                        .containsAll(List.of(1234567890123456L))
                        .containsAny(List.of(1L, 1234567890123456L, 2L))
                        .anySatisfy(val -> Assertions.assertEquals(1234567890123456L, val))
                        .allSatisfy(val -> Assertions.assertEquals(1234567890123456L, val))
                        .satisfy(vals -> Assertions.assertEquals(1, vals.size())))
                .decimalArrayPath("$.decimalArray[*]", decimalVals -> decimalVals
                        .hasSize(2)
                        .containsAll(List.of(new BigDecimal("123456789.123456789"), new BigDecimal("123456789.223456789")))
                        .containsAny(List.of(BigDecimal.ZERO, new BigDecimal("123456789.223456789")))
                        .anySatisfy(val -> Assertions.assertTrue(val.compareTo(new BigDecimal("123456789.123456789")) == 0))
                        .allSatisfy(val -> Assertions.assertTrue(val.compareTo(BigDecimal.ZERO) > 0))
                        .satisfy(vals -> Assertions.assertEquals(2, vals.size())));
    }

    @Nested
    class isTrue {

        static JsonAssured.JsonPathAssertions subject = JsonAssured.assertJson(jsonAllTypes);

        @Test
        void when_val_is_false__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.booleanFalse"));
            Assertions.assertEquals("Expected value at path \"$.booleanFalse\" to be true, but actual value was false", assertionError.getMessage());
        }

        @Test
        void when_val_is_object__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.objectVal"));
            Assertions.assertEquals("Expected value at path \"$.objectVal\" to be true, but actual value was {nestedStringVal=str, nestedIntVal=123456789}",
                    assertionError.getMessage());
        }

        @Test
        void when_val_is_0__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.zeroIntVal"));
            Assertions.assertEquals("Expected value at path \"$.zeroIntVal\" to be true, but actual value was 0",
                    assertionError.getMessage());
        }

        @Test
        void when_val_is_null__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.nullVal"));
            Assertions.assertEquals("Expected value at path \"$.nullVal\" to be true, but actual value was null",
                    assertionError.getMessage());
        }
    }

    @Nested
    class isFalse {

        static JsonAssured.JsonPathAssertions subject = JsonAssured.assertJson(jsonAllTypes);

        @Test
        void when_val_is_true__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.booleanTrue"));
            Assertions.assertEquals("Expected value at path \"$.booleanTrue\" to be false, but actual value was true", assertionError.getMessage());
        }

        @Test
        void when_val_is_object__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.objectVal"));
            Assertions.assertEquals("Expected value at path \"$.objectVal\" to be false, but actual value was {nestedStringVal=str, nestedIntVal=123456789}",
                    assertionError.getMessage());
        }

        @Test
        void when_val_is_0__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.zeroIntVal"));
            Assertions.assertEquals("Expected value at path \"$.zeroIntVal\" to be false, but actual value was 0",
                    assertionError.getMessage());
        }

        @Test
        void when_val_is_null__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.nullVal"));
            Assertions.assertEquals("Expected value at path \"$.nullVal\" to be false, but actual value was null",
                    assertionError.getMessage());
        }
    }

    @Nested
    class isNull {

        static JsonAssured.JsonPathAssertions subject = JsonAssured.assertJson(jsonAllTypes);

        @Test
        void when_val_is_true__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isNull("$.booleanTrue"));
            Assertions.assertEquals("Expected value at path \"$.booleanTrue\" to be null, but actual value was true", assertionError.getMessage());
        }

        @Test
        void when_val_is_object__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isNull("$.objectVal"));
            Assertions.assertEquals("Expected value at path \"$.objectVal\" to be null, but actual value was {nestedStringVal=str, nestedIntVal=123456789}",
                    assertionError.getMessage());
        }
    }

    @Nested
    class isNotNull {

        static JsonAssured.JsonPathAssertions subject = JsonAssured.assertJson(jsonAllTypes);

        @Test
        void when_val_is_null__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.isNotNull("$.nullVal"));
            Assertions.assertEquals("Expected value at path \"$.nullVal\" to be non-null, but actual value was null", assertionError.getMessage());
        }

        @Test
        void when_val_is_not_null__then_ok() {
            Assertions.assertDoesNotThrow(() -> subject.isNotNull("$.stringVal"));
            Assertions.assertDoesNotThrow(() -> subject.isNotNull("$.objectVal"));
            Assertions.assertDoesNotThrow(() -> subject.isNotNull("$.zeroIntVal"));
        }
    }

    @Nested
    class stringPath {

        static JsonAssured.JsonPathAssertions subject = JsonAssured.assertJson(jsonAllTypes);

        @Test
        void when_int_type__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.stringPath("$.positiveIntVal", strVal -> strVal.isEqualTo("str2")));

            Assertions.assertEquals("Expected value of type string at path \"$.positiveIntVal\", but actual type was \"number\" (123456789)",
                    assertionError.getMessage());
        }

        @Test
        void when_null_type__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.stringPath("$.nullVal", strVal -> strVal.isEqualTo("str2")));

            Assertions.assertEquals("Expected value of type string at path \"$.nullVal\", but actual type was \"null\"",
                    assertionError.getMessage());
        }

        @Test
        void when_string_array_type__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.stringPath("$.stringsArray", strVal -> strVal.isEqualTo("str2")));

            Assertions.assertEquals("Expected value of type string at path \"$.stringsArray\", but actual type was \"array\" ([\"a\",\"b\",\"c\"])",
                    assertionError.getMessage());
        }

        @Test
        void when_object_type__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.stringPath("$.objectVal", strVal -> strVal.isEqualTo("str2")));

            Assertions.assertEquals("Expected value of type string at path \"$.objectVal\", but actual type was \"object\" ({nestedStringVal=str, nestedIntVal=123456789})",
                    assertionError.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"str", "STR", "qwe", "sTR"})
        void isEqualTo__when_not_equal__then_fail(String expected) {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.stringPath("$.stringVal", strVal -> strVal.isEqualTo(expected)));

            Assertions.assertEquals(String.format("String value at path \"$.stringVal\" are not equal: Expected: <%s> but was: <sTr>", expected), assertionError.getMessage());
        }

        @Test
        void isNotEqualTo__when_equal__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.stringPath("$.stringVal", strVal -> strVal.isNotEqualTo("sTr")));

            Assertions.assertEquals("String value at path \"$.stringVal\" is equal to <sTr>, while expected to be not equal", assertionError.getMessage());
        }
    }

    @Nested
    class doesNotExist {

        static JsonAssured.JsonPathAssertions subject = JsonAssured.assertJson(jsonAllTypes);

        @Test
        void when_not_exists__then_ok() {
            Assertions.assertDoesNotThrow(() -> subject.doesNotExist("$.asdvz"));
        }

        @Test
        void when_exists_and_null__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.doesNotExist("$.nullVal"));

            Assertions.assertEquals("Expected value at path \"$.nullVal\" to be absent, but found <null>", assertionError.getMessage());
        }

        @Test
        void when_exists_and_object__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.doesNotExist("$.objectVal"));

            Assertions.assertEquals("Expected value at path \"$.objectVal\" to be absent, but found <{nestedStringVal=str, nestedIntVal=123456789}>", assertionError.getMessage());
        }

        @Test
        void when_exists_and_0__then_fail() {
            var assertionError = Assertions.assertThrows(AssertionError.class, () -> subject.doesNotExist("$.zeroIntVal"));

            Assertions.assertEquals("Expected value at path \"$.zeroIntVal\" to be absent, but found <0>", assertionError.getMessage());
        }
    }
}

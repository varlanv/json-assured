package com.varlanv.jsonassured;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

class JsonAssuredTest {

  @Language("json")
  private static final String jsonAllTypes =
      """
                    {
                    "blankStringVal": " \\n \\t ",
                    "emptyStringVal": "",
                    "stringVal": "sTr",
                    "zeroIntVal": 0,
                    "zeroDecimalVal": 0.0,
                    "smallDecimalVal": 1.2,
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

  static JsonAssured.JsonPathAssertions subject = JsonAssured.assertJson(jsonAllTypes);

  static Stream<JsonAssured.JsonPathAssertions> test_happy() {
    var bytes = jsonAllTypes.getBytes(StandardCharsets.UTF_8);
    return Stream.of(
        JsonAssured.assertJson(jsonAllTypes),
        JsonAssured.assertJson(bytes),
        JsonAssured.assertJson(new StringReader(jsonAllTypes)),
        JsonAssured.assertJson(new ByteArrayInputStream(bytes)));
  }

  @MethodSource
  @ParameterizedTest
  void test_happy(JsonAssured.JsonPathAssertions subject) {
    subject
        .isTrue("$.booleanTrue")
        .isFalse("$.booleanFalse")
        .isNull("$.nullVal")
        .isNotNull("$.objectVal")
        .isNotNull("$.stringsArray")
        .isNotNull("$.objectsArray")
        .isNotNull("$.booleanFalse")
        .isNotNull("$.stringVal")
        .isNotNull("$.zeroIntVal")
        .isNotNull("$.decimalAsStringVal")
        .isNotNull("$.booleanTrue")
        .isEqual("$.stringVal", "sTr")
        .stringPath(
            "$.blankStringVal", strVal -> strVal.isBlank().isNotEmpty().isEqualTo(" \n \t "))
        .stringPath("$.emptyStringVal", strVal -> strVal.isEmpty().isBlank().isEqualTo(""))
        .stringPath(
            "$.stringVal",
            strVal ->
                strVal
                    .isEqualTo("sTr")
                    .isNotBlank()
                    .isNotEmpty()
                    .isNotEqualTo("str")
                    .isEqualToIgnoringCase("str")
                    .isNotEqualToIgnoringCase("st")
                    .contains("s")
                    .contains("r")
                    .contains("T")
                    .contains("sT")
                    .containsIgnoringCase("str")
                    .containsIgnoringCase("tr")
                    .containsIgnoringCase("st")
                    .hasLength(3)
                    .hasLengthRange(2, 4)
                    .hasLengthRange(3, 3)
                    .hasLengthRange(3, 5)
                    .hasLengthRange(2, 3)
                    .hasLengthAtLeast(1)
                    .hasLengthAtLeast(2)
                    .hasLengthAtLeast(3)
                    .hasLengthAtMost(3)
                    .hasLengthAtMost(4)
                    .hasLengthAtMost(5)
                    .isIn(List.of("sTr", "stTr", "sT"))
                    .isIn(List.of("sTr"))
                    .isIn(List.of("sTr", "stTr", "sT"))
                    .isNotIn(List.of("str", "stTr", "sT"))
                    .matches("^[a-zA-Z]+$")
                    .matches("sTr")
                    .matches("s[a-zA-Z]+")
                    .doesNotMatch("^[0-9]+$")
                    .satisfies(it -> {}))
        .intPath(
            "$.zeroIntVal",
            intVal ->
                intVal
                    .isZero()
                    .isEqualTo(0)
                    .isNotEqualTo(1)
                    .isNotEqualTo(-1)
                    .isIn(List.of(0))
                    .isIn(List.of(-1, 0, 1))
                    .isLte(0)
                    .isLte(1)
                    .isLte(123456789)
                    .isGte(0)
                    .isGte(-1)
                    .isGte(-123456789))
        .intPath(
            "$.positiveIntVal",
            intVal ->
                intVal
                    .isEqualTo(123456789)
                    .isNotEqualTo(12345678)
                    .isPositive()
                    .isGte(-1)
                    .isGte(0)
                    .isGte(1)
                    .isGte(123456789)
                    .isLte(123456789)
                    .isLte(1234567890)
                    .isInRange(-1, 1234567890)
                    .isInRange(-1, 123456789)
                    .isNotIn(List.of(12345678, 1234567890))
                    .satisfies(it -> {})
                    .isIn(List.of(123456789, 1234567890)))
        .decimalPath(
            "$.zeroDecimalVal",
            decimalVal ->
                decimalVal
                    .isZero()
                    .isGte(BigDecimal.valueOf(-1))
                    .isGte(BigDecimal.valueOf(0))
                    .isLte(BigDecimal.valueOf(0))
                    .isGte(BigDecimal.valueOf(-123456789))
                    .isIn(
                        List.of(
                            BigDecimal.valueOf(-1), BigDecimal.valueOf(0.0), BigDecimal.valueOf(1)))
                    .isNotIn(
                        List.of(
                            BigDecimal.valueOf(-123456789),
                            BigDecimal.valueOf(123456789),
                            BigDecimal.ZERO,
                            BigDecimal.valueOf(0)))
                    .satisfies(it -> {}))
        .decimalPath(
            "$.positiveDecimalVal",
            decimalVal ->
                decimalVal
                    .isEqualTo(new BigDecimal("123456789.123456789"))
                    .isNotEqualTo(new BigDecimal("123456789.123456788"))
                    .isGte(new BigDecimal("123456789.123456789"))
                    .isGte(BigDecimal.valueOf(-123456789.123456789))
                    .isGte(BigDecimal.ZERO)
                    .isGte(BigDecimal.ONE)
                    .isLte(new BigDecimal("123456789.123456789"))
                    .isLte(new BigDecimal("123456789.223456789"))
                    .isPositive()
                    .isInRange(BigDecimal.valueOf(123456789), BigDecimal.valueOf(123456790))
                    .isIn(List.of(new BigDecimal("123456789.123456789")))
                    .isNotIn(List.of(BigDecimal.ZERO))
                    .satisfies(d -> {}))
        .stringPath("$.intAsStringVal", strVal -> strVal.isEqualTo("1234").matches("^[0-9]+$"))
        .stringPath(
            "$.decimalAsStringVal",
            strVal -> strVal.isEqualTo("1234.5678").matches("^[0-9]+(\\.[0-9]+)?$"))
        .intPath(
            "$.negativeIntVal",
            intVal ->
                intVal.isNegative().isEqualTo(-123456789).isLte(-123456789).isLte(-1).isLte(0))
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
        .stringArrayPath(
            "$.stringsArray[*]",
            strVals ->
                strVals
                    .hasSize(3)
                    .anySatisfy(val -> Assertions.assertEquals("a", val))
                    .allSatisfy(val -> Assertions.assertEquals(1, val.length()))
                    .satisfy(vals -> Assertions.assertEquals(3, vals.size())))
        .intArrayPath(
            "$.intArray[*]",
            intVals ->
                intVals
                    .hasSize(3)
                    .containsAll(List.of(1, 2, 3))
                    .containsAny(List.of(0, 2, 5))
                    .anySatisfy(val -> Assertions.assertEquals(1, val))
                    .allSatisfy(val -> Assertions.assertTrue(val < 10))
                    .satisfy(vals -> Assertions.assertEquals(3, vals.size())))
        .longArrayPath(
            "$.longArray[*]",
            longVals ->
                longVals
                    .hasSize(1)
                    .containsAll(List.of(1234567890123456L))
                    .containsAny(List.of(1L, 1234567890123456L, 2L))
                    .anySatisfy(val -> Assertions.assertEquals(1234567890123456L, val))
                    .allSatisfy(val -> Assertions.assertEquals(1234567890123456L, val))
                    .satisfy(vals -> Assertions.assertEquals(1, vals.size())))
        .decimalArrayPath(
            "$.decimalArray[*]",
            decimalVals ->
                decimalVals
                    .hasSize(2)
                    .containsAll(
                        List.of(
                            new BigDecimal("123456789.123456789"),
                            new BigDecimal("123456789.223456789")))
                    .containsAny(List.of(BigDecimal.ZERO, new BigDecimal("123456789.223456789")))
                    .anySatisfy(
                        val ->
                            Assertions.assertEquals(
                                0, val.compareTo(new BigDecimal("123456789.123456789"))))
                    .allSatisfy(val -> Assertions.assertTrue(val.compareTo(BigDecimal.ZERO) > 0))
                    .satisfy(vals -> Assertions.assertEquals(2, vals.size())));
  }

  @Nested
  class isTrue {

    @Test
    void when_val_is_false__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.booleanFalse"));
      Assertions.assertEquals(
          "Expected value at path \"$.booleanFalse\" to be true, but actual value was false",
          assertionError.getMessage());
    }

    @Test
    void when_val_is_object__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.objectVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.objectVal\" to be true, but actual value was {nestedStringVal=str, nestedIntVal=123456789}",
          assertionError.getMessage());
    }

    @Test
    void when_val_is_0__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.zeroIntVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.zeroIntVal\" to be true, but actual value was 0",
          assertionError.getMessage());
    }

    @Test
    void when_val_is_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isTrue("$.nullVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.nullVal\" to be true, but actual value was null",
          assertionError.getMessage());
    }
  }

  @Nested
  class isFalse {

    @Test
    void when_val_is_true__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.booleanTrue"));
      Assertions.assertEquals(
          "Expected value at path \"$.booleanTrue\" to be false, but actual value was true",
          assertionError.getMessage());
    }

    @Test
    void when_val_is_object__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.objectVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.objectVal\" to be false, but actual value was {nestedStringVal=str, nestedIntVal=123456789}",
          assertionError.getMessage());
    }

    @Test
    void when_val_is_0__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.zeroIntVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.zeroIntVal\" to be false, but actual value was 0",
          assertionError.getMessage());
    }

    @Test
    void when_val_is_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isFalse("$.nullVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.nullVal\" to be false, but actual value was null",
          assertionError.getMessage());
    }
  }

  @Nested
  class isNull {

    @Test
    void when_val_is_true__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isNull("$.booleanTrue"));
      Assertions.assertEquals(
          "Expected value at path \"$.booleanTrue\" to be null, but actual value was <true>",
          assertionError.getMessage());
    }

    @Test
    void when_val_is_object__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isNull("$.objectVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.objectVal\" to be null, but actual value was <{nestedStringVal=str, nestedIntVal=123456789}>",
          assertionError.getMessage());
    }
  }

  @Nested
  class isEqual_string {

    @ParameterizedTest
    @ArgumentsSource(NullableBlankStrings.class)
    void when_blank_jsonPath__then_fail(@Language("jsonpath") String string) {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class, () -> subject.isEqual(string, "str"));
      Assertions.assertEquals(
          "jsonPath should be non-null and non-blank", assertionError.getMessage());
    }

    @Test
    void when_not_equal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class, () -> subject.isEqual("$.stringVal", "str"));
      Assertions.assertEquals(
          "String value at path \"$.stringVal\" are not equal: Expected: <str> but was: <sTr>",
          assertionError.getMessage());
    }

    @Test
    void when_actual_is_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isEqual("$.nullVal", "str"));
      Assertions.assertEquals(
          "Expected value of type string at path \"$.nullVal\", but actual type was \"null\"",
          assertionError.getMessage());
    }

    @Test
    void when_expected_is_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class, () -> subject.isEqual("$.nullVal", null));
      Assertions.assertEquals(
          "\"null\" expected values are not supported. Consider using `JsonPathAssertions#isNull()` instead",
          assertionError.getMessage());
    }

    @Test
    void when_actual_is_number__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class, () -> subject.isEqual("$.positiveIntVal", "str"));
      Assertions.assertEquals(
          "Expected value of type string at path \"$.positiveIntVal\", but actual type was \"integer\" (123456789)",
          assertionError.getMessage());
    }

    @Test
    void when_actual_is__object__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class, () -> subject.isEqual("$.objectVal", "str"));
      Assertions.assertEquals(
          "Expected value of type string at path \"$.objectVal\", but actual type was \"object\" ({nestedStringVal=str, nestedIntVal=123456789})",
          assertionError.getMessage());
    }

    @Test
    void when_actual_is__array__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class, () -> subject.isEqual("$.stringsArray", "str"));
      Assertions.assertEquals(
          "Expected value of type string at path \"$.stringsArray\", but actual type was \"array\" ([\"a\",\"b\",\"c\"])",
          assertionError.getMessage());
    }
  }

  @Nested
  class isNotNull {

    @Test
    void when_val_is_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.isNotNull("$.nullVal"));
      Assertions.assertEquals(
          "Expected value at path \"$.nullVal\" to be non-null, but actual value was null",
          assertionError.getMessage());
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

    @Test
    void when_int_type__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.positiveIntVal", strVal -> strVal.isEqualTo("str2")));

      Assertions.assertEquals(
          "Expected value of type string at path \"$.positiveIntVal\", but actual type was \"integer\" (123456789)",
          assertionError.getMessage());
    }

    @Test
    void when_null_type__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.nullVal", strVal -> strVal.isEqualTo("str2")));

      Assertions.assertEquals(
          "Expected value of type string at path \"$.nullVal\", but actual type was \"null\"",
          assertionError.getMessage());
    }

    @Test
    void when_string_array_type__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringsArray", strVal -> strVal.isEqualTo("str2")));

      Assertions.assertEquals(
          "Expected value of type string at path \"$.stringsArray\", but actual type was \"array\" ([\"a\",\"b\",\"c\"])",
          assertionError.getMessage());
    }

    @Test
    void when_object_type__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.objectVal", strVal -> strVal.isEqualTo("str2")));

      Assertions.assertEquals(
          "Expected value of type string at path \"$.objectVal\", but actual type was \"object\" ({nestedStringVal=str, nestedIntVal=123456789})",
          assertionError.getMessage());
    }

    @ParameterizedTest
    @ArgumentsSource(NullableBlankStrings.class)
    void isEqualTo__when_blank_jsonPath__then_fail(@Language("jsonpath") String blankString) {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.stringPath(blankString, strVal -> strVal.isEqualTo("sTr")));

      Assertions.assertEquals(
          "jsonPath should be non-null and non-blank", assertionError.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"str", "STR", "qwe", "sTR"})
    void isEqualTo__when_not_equal__then_fail(String expected) {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", strVal -> strVal.isEqualTo(expected)));

      Assertions.assertEquals(
          String.format(
              "String value at path \"$.stringVal\" is not equal to expected: Expected: <%s> but was: <sTr>",
              expected),
          assertionError.getMessage());
    }

    @Test
    void isNotEqualTo__when_equal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", strVal -> strVal.isNotEqualTo("sTr")));

      Assertions.assertEquals(
          "String value at path \"$.stringVal\" is equal to <sTr>, while expected to be not equal",
          assertionError.getMessage());
    }

    @Test
    void isEqualToIgnoringCase__when_not_equal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath(
                      "$.stringVal", strVal -> strVal.isEqualToIgnoringCase("sTrq")));

      Assertions.assertEquals(
          "String value at path \"$.stringVal\" is not equal to expected (ignoring case): Expected: <sTrq> but was: <sTr>",
          assertionError.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"str", "STR", "sTR"})
    void isNotEqualToIgnoringCase__when_not_equal__then_fail(String expected) {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath(
                      "$.stringVal", strVal -> strVal.isNotEqualToIgnoringCase(expected)));

      Assertions.assertEquals(
          String.format(
              "String value at path \"$.stringVal\" is equal to <%s> (ignoring case), while expected to be not equal",
              expected),
          assertionError.getMessage());
    }

    @ParameterizedTest
    @ArgumentsSource(NonNullBlankStrings.class)
    void isBlank__when_blank__then_ok(String blankString) {
      @Language("json")
      var jsonWithBlank = "{" + "\"stringVal\": \"" + blankString + "\"" + "}";
      var subject = JsonAssured.assertJson(jsonWithBlank);

      Assertions.assertDoesNotThrow(
          () -> subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isBlank));
    }

    @Test
    void isBlank__when_not_blank__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isBlank));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to be blank, but actual value was \"sTr\"",
          assertionError.getMessage());
    }

    @ParameterizedTest
    @ArgumentsSource(NonNullBlankStrings.class)
    void isNotBlank__when_blank__then_fail(String blankString) {
      @Language("json")
      var jsonWithBlank = "{" + "\"stringVal\": \"" + blankString + "\"" + "}";
      var subject = JsonAssured.assertJson(jsonWithBlank);

      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isNotBlank));

      Assertions.assertEquals(
          String.format(
              "Expected string at path \"$.stringVal\" to be not blank, but actual value was \"%s\"",
              blankString),
          assertionError.getMessage());
    }

    @Test
    void isNotBlank__when_not_blank__then_ok() {
      Assertions.assertDoesNotThrow(
          () -> subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isNotBlank));
    }

    @Test
    void isEmpty__when_empty__then_ok() {
      @Language("json")
      var jsonWithEmpty = "{" + "\"stringVal\": \"" + "\"" + "}";
      var subject = JsonAssured.assertJson(jsonWithEmpty);

      Assertions.assertDoesNotThrow(
          () -> subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isEmpty));
    }

    @Test
    void isEmpty__when_not_empty__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isEmpty));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to be empty, but actual value was <sTr>",
          assertionError.getMessage());
    }

    @Test
    void isNotEmpty__when_empty__then_fail() {
      @Language("json")
      var jsonWithBlank = "{" + "\"stringVal\": \"" + "\"" + "}";
      var subject = JsonAssured.assertJson(jsonWithBlank);

      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isNotEmpty));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to be not empty, but actual value was empty",
          assertionError.getMessage());
    }

    @Test
    void isNotEmpty__when_not_empty__then_ok() {
      Assertions.assertDoesNotThrow(
          () -> subject.stringPath("$.stringVal", JsonAssured.JsonStringAssertions::isNotEmpty));
    }

    @Test
    void hasLength__when_not_matches__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLength(10)));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to have length [10], but actual length was [3]",
          assertionError.getMessage());
    }

    @Test
    void hasLength__when_length_is_negative__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLength(-1)));

      Assertions.assertEquals(
          "Length cannot be negative (received -1)", assertionError.getMessage());
    }

    @Test
    void hasLength__when_length_matches__then_ok() {
      Assertions.assertDoesNotThrow(
          () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLength(3)));
    }

    @Test
    void hasLengthRange__when_range_matches__then_ok() {
      Assertions.assertDoesNotThrow(
          () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthRange(2, 3)));
    }

    @Test
    void hasLengthRange__when_min_length_is_negative__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () ->
                  subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthRange(-2, 10)));

      Assertions.assertEquals(
          "Min length cannot be negative (received -2)", assertionError.getMessage());
    }

    @Test
    void hasLengthRange__when_max_length_is_negative__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () ->
                  subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthRange(10, -1)));

      Assertions.assertEquals(
          "Max length cannot be negative (received -1)", assertionError.getMessage());
    }

    @Test
    void hasLengthRange__when_max_less_than_min__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthRange(1, 0)));

      Assertions.assertEquals(
          "Min length cannot be greater than max length (received 1 > 0)",
          assertionError.getMessage());
    }

    @Test
    void hasLengthRange__when__lower_mismatch__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthRange(1, 2)));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to be in range [1 - 2], but actual length was [3]",
          assertionError.getMessage());
    }

    @Test
    void hasLengthRange__when__upper_mismatch__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthRange(4, 5)));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to be in range [4 - 5], but actual length was [3]",
          assertionError.getMessage());
    }

    @Test
    void hasLengthAtLeast__when_min_length_is_negative__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthAtLeast(-1)));

      Assertions.assertEquals(
          "Min length cannot be negative (received -1)", assertionError.getMessage());
    }

    @Test
    void hasLengthAtLeast__when_not_matches__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthAtLeast(4)));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to have min length [3], but actual length was [4]",
          assertionError.getMessage());
    }

    @Test
    void hasLengthAtMost__when_min_length_is_negative__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthAtMost(-10)));

      Assertions.assertEquals(
          "Max length cannot be negative (received -10)", assertionError.getMessage());
    }

    @Test
    void hasLengthAtMost__when_not_matches__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.hasLengthAtMost(2)));

      Assertions.assertEquals(
          "Expected string at path \"$.stringVal\" to have max length [3], but actual length was [2]",
          assertionError.getMessage());
    }

    @Test
    void contains__when_not_contain__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.contains("str")));

      Assertions.assertEquals(
          "String value at path \"$.stringVal\" does not contain expected string: Expected: <str> but was: <sTr>",
          assertionError.getMessage());
    }

    @Test
    void containsIgnoringCase__when_not_contain__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath(
                      "$.stringVal", stringVal -> stringVal.containsIgnoringCase("abcd")));

      Assertions.assertEquals(
          "String value at path \"$.stringVal\" does not contain expected string (ignoring case): Expected: <abcd> but was: <sTr>",
          assertionError.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sTrr", ".qweq"})
    void matches__when_not_matches__then_fail(@Language("regexp") String pattern) {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.stringPath("$.stringVal", stringVal -> stringVal.matches(pattern)));

      Assertions.assertEquals(
          String.format(
              "String value at path \"$.stringVal\" does not match expected pattern. Expected pattern: <%s>, actual value: <sTr>",
              pattern),
          assertionError.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sTr", ".*"})
    void doesNotMatch__when_matches__then_fail(@Language("regexp") String pattern) {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath("$.stringVal", stringVal -> stringVal.doesNotMatch(pattern)));

      Assertions.assertEquals(
          String.format(
              String.format(
                  "String value at path \"$.stringVal\" matches expected pattern, while expected to not match. Pattern: <%s>, actual value: <sTr>",
                  pattern),
              pattern),
          assertionError.getMessage());
    }

    @Test
    void isIn__when_not_in__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath(
                      "$.stringVal", stringVal -> stringVal.isIn(List.of("a", "str", "b"))));

      Assertions.assertEquals(
          "String value at path \"$.stringVal\" is not in the list of expected values. Actual value: <sTr>, list of expected values: <[a, str, b]>",
          assertionError.getMessage());
    }

    @Test
    void isIn__when_charSequences_in__then_ok() {
      Assertions.assertDoesNotThrow(
          () ->
              subject.stringPath(
                  "$.stringVal",
                  stringVal ->
                      stringVal.isIn(
                          List.of(new StringBuilder("q"), new StringBuffer("sTr"), "qwe"))));
    }

    @Test
    void isIn_when_input_contains_null__then_fail() {
      var in = new ArrayList<String>();
      in.add("qwe");
      in.add(null);
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.stringPath("$.stringVal", strVal -> strVal.isIn(in)));

      Assertions.assertEquals(
          "Array of expected values cannot contain null elements, but found null element at index [1]",
          assertionError.getMessage());
    }

    @Test
    void isNotIn__when_in__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath(
                      "$.stringVal",
                      stringVal -> stringVal.isNotIn(List.of("a", "str", "sTr", "b"))));

      Assertions.assertEquals(
          "String value at path \"$.stringVal\" was found in provided list at index [2]. Actual value: <sTr>, list of values: <[a, str, sTr, b]>",
          assertionError.getMessage());
    }

    @Test
    void satisfies_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.stringPath("$.stringVal", strVal -> strVal.satisfies(null)));

      Assertions.assertEquals("'Consumer' value cannot be null", assertionError.getMessage());
    }

    @Test
    void satisfies__when_not_satisfy_then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.stringPath(
                      "$.stringVal",
                      stringVal ->
                          stringVal.satisfies(val -> Assertions.assertEquals("str", val))));

      Assertions.assertEquals(
          "String value at path \"$.stringVal\" did not satisfy provided condition",
          assertionError.getMessage());
      Assertions.assertEquals(
          "expected: <str> but was: <sTr>", assertionError.getCause().getMessage());
    }
  }

  @Nested
  class doesNotExist {

    @ParameterizedTest
    @ArgumentsSource(NullableBlankStrings.class)
    void when_null_jsonPath__then_fail(@Language("jsonpath") String string) {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class, () -> subject.doesNotExist(string));
      Assertions.assertEquals(
          "jsonPath should be non-null and non-blank", assertionError.getMessage());
    }

    @Test
    void when_not_exists__then_ok() {
      Assertions.assertDoesNotThrow(() -> subject.doesNotExist("$.asdvz"));
    }

    @Test
    void when_null_path__then_fail() {
      var assertionError =
          Assertions.assertThrows(IllegalArgumentException.class, () -> subject.doesNotExist(null));

      Assertions.assertEquals(
          "jsonPath should be non-null and non-blank", assertionError.getMessage());
    }

    @Test
    void when_exists_and_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.doesNotExist("$.nullVal"));

      Assertions.assertEquals(
          "Expected value at path \"$.nullVal\" to be absent, but found <null>",
          assertionError.getMessage());
    }

    @Test
    void when_exists_and_object__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.doesNotExist("$.objectVal"));

      Assertions.assertEquals(
          "Expected value at path \"$.objectVal\" to be absent, but found <{nestedStringVal=str, nestedIntVal=123456789}>",
          assertionError.getMessage());
    }

    @Test
    void when_exists_and_0__then_fail() {
      var assertionError =
          Assertions.assertThrows(AssertionError.class, () -> subject.doesNotExist("$.zeroIntVal"));

      Assertions.assertEquals(
          "Expected value at path \"$.zeroIntVal\" to be absent, but found <0>",
          assertionError.getMessage());
    }
  }

  static class NullableBlankStrings implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        throws Exception {
      return Stream.concat(NonNullBlankStrings.strings(), Stream.builder().add(null).build())
          .map(Arguments::of);
    }
  }

  @Nested
  class intPath {

    @Test
    void isNegative__when_string_value__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.stringVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.stringVal\", but actual type was \"string\": <sTr>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.nullVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.nullVal\", but actual type was \"null\"",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_array__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath("$.stringsArray", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.stringsArray\", but actual type was \"array\": <[\"a\",\"b\",\"c\"]>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_object__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.objectVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.objectVal\", but actual type was \"object\": <{nestedStringVal=str, nestedIntVal=123456789}>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_decimal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.positiveDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.positiveDecimalVal\", but actual type was \"decimal\": <123456789.123456789>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_long__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.positiveLongVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.positiveLongVal\", but actual type was \"long\": <1234567890123456>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_small_decimal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.smallDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.smallDecimalVal\", but actual type was \"decimal\": <1.2>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_boolean__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.booleanTrue", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.booleanTrue\", but actual type was \"boolean\": <true>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_zero_decimal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.zeroDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.zeroDecimalVal\", but actual type was \"decimal\": <0.0>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_int_as_string__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.intAsStringVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Integer at path \"$.intAsStringVal\", but actual type was \"string\": <1234>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_positive__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.positiveIntVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected Int number at path \"$.positiveIntVal\" to be negative, but actual value was <123456789>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_zero__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.zeroIntVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected Int number at path \"$.zeroIntVal\" to be negative, but actual value was <0>",
          assertionError.getMessage());
    }

    @Test
    void isPositive__when_negative__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.negativeIntVal", JsonAssured.JsonNumberAssertions::isPositive));

      Assertions.assertEquals(
          "Expected Int number at path \"$.negativeIntVal\" to be positive, but actual value was <-123456789>",
          assertionError.getMessage());
    }

    @Test
    void isPositive__when_zero__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.zeroIntVal", JsonAssured.JsonNumberAssertions::isPositive));

      Assertions.assertEquals(
          "Expected Int number at path \"$.zeroIntVal\" to be positive, but actual value was <0>",
          assertionError.getMessage());
    }

    @Test
    void isZero__when_positive__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.positiveIntVal", JsonAssured.JsonNumberAssertions::isZero));

      Assertions.assertEquals(
          "Expected Int number at path \"$.positiveIntVal\" to be zero, but actual value was <123456789>",
          assertionError.getMessage());
    }

    @Test
    void isZero__when_negative__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.negativeIntVal", JsonAssured.JsonNumberAssertions::isZero));

      Assertions.assertEquals(
          "Expected Int number at path \"$.negativeIntVal\" to be zero, but actual value was <-123456789>",
          assertionError.getMessage());
    }

    @Test
    void isEqualTo__when_equal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isEqualTo(123)));

      Assertions.assertEquals(
          "Expected Int number at path \"$.negativeIntVal\" to be equal <123>, but actual value was <-123456789>",
          assertionError.getMessage());
    }

    @Test
    void isEqualTo_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isEqualTo(null)));

      Assertions.assertEquals("Expected value cannot be null", assertionError.getMessage());
    }

    @Test
    void isNotEqualTo_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isNotEqualTo(null)));

      Assertions.assertEquals("Expected value cannot be null", assertionError.getMessage());
    }

    @Test
    void isNotEqualTo_when_equal_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isNotEqualTo(-123456789)));

      Assertions.assertEquals(
          "Expected Int number at path \"$.negativeIntVal\" to not be equal <-123456789>, but were equal",
          assertionError.getMessage());
    }

    @Test
    void isGte_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isGte(null)));

      Assertions.assertEquals("Expected value cannot be null", assertionError.getMessage());
    }

    @Test
    void isGte_when_not_gte__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isGte(1)));

      Assertions.assertEquals(
          "Expected Int number at path \"$.negativeIntVal\" to be greater than or equal to <1>, but was <-123456789>",
          assertionError.getMessage());
    }

    @Test
    void isLte_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isLte(null)));

      Assertions.assertEquals("Expected value cannot be null", assertionError.getMessage());
    }

    @Test
    void isLte_when_not_lte__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isLte(-1234567890)));

      Assertions.assertEquals(
          "Expected Int number at path \"$.negativeIntVal\" to be less than or equal to <-1234567890>, but was <-123456789>",
          assertionError.getMessage());
    }

    @Test
    void isInRange_when_null_min__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isInRange(null, 1)));

      Assertions.assertEquals("'Min' value cannot be null", assertionError.getMessage());
    }

    @Test
    void isInRange_when_null_max__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isInRange(1, null)));

      Assertions.assertEquals("'Max' value cannot be null", assertionError.getMessage());
    }

    @Test
    void isInRange_when_bottom_higher_than_top__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.negativeIntVal", intVal -> intVal.isInRange(2, 1)));

      Assertions.assertEquals(
          "Min value should be less than or equal to max value, but received min <2> and max <1>",
          assertionError.getMessage());
    }

    @Test
    void isInRange_when_not_in_range_bottom__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.positiveIntVal", intVal -> intVal.isInRange(123456787, 123456788)));

      Assertions.assertEquals(
          "Expected Int number at path \"$.positiveIntVal\" to be in range [123456787 - 123456788], but was <123456789>",
          assertionError.getMessage());
    }

    @Test
    void isInRange_when_not_in_range_top__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.positiveIntVal", intVal -> intVal.isInRange(123456790, 123456791)));

      Assertions.assertEquals(
          "Expected Int number at path \"$.positiveIntVal\" to be in range [123456790 - 123456791], but was <123456789>",
          assertionError.getMessage());
    }

    @Test
    void isIn_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.positiveIntVal", intVal -> intVal.isIn(null)));

      Assertions.assertEquals("Expected value cannot be null", assertionError.getMessage());
    }

    @Test
    void isIn_when_input_contains_null__then_fail() {
      var in = new ArrayList<Integer>();
      in.add(123456789);
      in.add(null);
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.positiveIntVal", intVal -> intVal.isIn(in)));

      Assertions.assertEquals(
          "Array of expected values cannot contain null elements, but found null element at index [1]",
          assertionError.getMessage());
    }

    @Test
    void isIn_when_not_in__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.intPath("$.positiveIntVal", intVal -> intVal.isIn(List.of(1, 2))));

      Assertions.assertEquals(
          "Int number at path \"$.positiveIntVal\" is not in the list of expected values. "
              + "Actual value: <123456789>, list of expected values: [1, 2]",
          assertionError.getMessage());
    }

    @Test
    void isNotIn_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.positiveIntVal", intVal -> intVal.isNotIn(null)));

      Assertions.assertEquals("Expected value cannot be null", assertionError.getMessage());
    }

    @Test
    void isNotIn_when__in__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.positiveIntVal", intVal -> intVal.isNotIn(List.of(1, 123456789, 2))));

      Assertions.assertEquals(
          "Int number value at path \"$.positiveIntVal\" was found in provided list at index [1]. "
              + "Actual value: <123456789>, list of values: <[1, 123456789, 2]>",
          assertionError.getMessage());
    }

    @Test
    void satisfies_when_null_input__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> subject.intPath("$.positiveIntVal", intVal -> intVal.satisfies(null)));

      Assertions.assertEquals("'Consumer' value cannot be null", assertionError.getMessage());
    }

    @Test
    void satisfies_when_not_satisfies__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.intPath(
                      "$.positiveIntVal",
                      intVal -> intVal.satisfies(val -> Assertions.assertEquals(1, val))));

      Assertions.assertEquals(
          "Int number value at path \"$.positiveIntVal\" did not satisfy provided condition",
          assertionError.getMessage());

      Assertions.assertEquals(
          "expected: <1> but was: <123456789>", assertionError.getCause().getMessage());
    }
  }

  @Nested
  class longPath {

    @Test
    void isNegative__when_string_value__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.longPath("$.stringVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.stringVal\", but actual type was \"string\": <sTr>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.longPath("$.nullVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.nullVal\", but actual type was \"null\"",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_array__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.longPath("$.stringsArray", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.stringsArray\", but actual type was \"array\": <[\"a\",\"b\",\"c\"]>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_object__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.longPath("$.objectVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.objectVal\", but actual type was \"object\": <{nestedStringVal=str, nestedIntVal=123456789}>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_decimal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.longPath(
                      "$.positiveDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.positiveDecimalVal\", but actual type was \"decimal\": <123456789.123456789>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_int__then_ok() {
      Assertions.assertDoesNotThrow(
          () -> subject.longPath("$.negativeIntVal", JsonAssured.JsonNumberAssertions::isNegative));
    }

    @Test
    void isNegative__when_small_decimal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.longPath(
                      "$.smallDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.smallDecimalVal\", but actual type was \"decimal\": <1.2>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_boolean__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.longPath("$.booleanTrue", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.booleanTrue\", but actual type was \"boolean\": <true>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_zero_decimal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.longPath(
                      "$.zeroDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.zeroDecimalVal\", but actual type was \"decimal\": <0.0>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_int_as_string__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.longPath(
                      "$.intAsStringVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Long at path \"$.intAsStringVal\", but actual type was \"string\": <1234>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_positive__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.longPath(
                      "$.positiveLongVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected Long number at path \"$.positiveLongVal\" to be negative, but actual value was <1234567890123456>",
          assertionError.getMessage());
    }
  }

  @Nested
  class decimalPath {

    @Test
    void isNegative__when_string_value__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath("$.stringVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.stringVal\", but actual type was \"string\": <sTr>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_null__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () -> subject.decimalPath("$.nullVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.nullVal\", but actual type was \"null\"",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_array__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath(
                      "$.stringsArray", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.stringsArray\", but actual type was \"array\": <[\"a\",\"b\",\"c\"]>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_object__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath("$.objectVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.objectVal\", but actual type was \"object\": <{nestedStringVal=str, nestedIntVal=123456789}>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_long__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath(
                      "$.positiveLongVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.positiveLongVal\", but actual type was \"long\": <1234567890123456>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_int_then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath(
                      "$.positiveIntVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.positiveIntVal\", but actual type was \"integer\": <123456789>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_boolean__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath(
                      "$.booleanTrue", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.booleanTrue\", but actual type was \"boolean\": <true>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_zero_decimal__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath(
                      "$.zeroDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected Decimal number at path \"$.zeroDecimalVal\" to be negative, but actual value was <0.0>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_int_as_string__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath(
                      "$.intAsStringVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected type Decimal at path \"$.intAsStringVal\", but actual type was \"string\": <1234>",
          assertionError.getMessage());
    }

    @Test
    void isNegative__when_positive__then_fail() {
      var assertionError =
          Assertions.assertThrows(
              AssertionError.class,
              () ->
                  subject.decimalPath(
                      "$.positiveDecimalVal", JsonAssured.JsonNumberAssertions::isNegative));

      Assertions.assertEquals(
          "Expected Decimal number at path \"$.positiveDecimalVal\" to be negative, but actual value was <123456789.123456789>",
          assertionError.getMessage());
    }
  }

  static class NonNullBlankStrings implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        throws Exception {
      return strings().map(Arguments::of);
    }

    static Stream<String> strings() {
      return Stream.of("", "  ", "\t", "\n");
    }
  }
}

package com.varlanv.jsonassured;

import java.util.function.Consumer;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

public interface WebFluxJsonAssured {

  static Consumer<EntityExchangeResult<byte[]>> jsonPathSpec(
      JsonAssured.ThrowingConsumer<JsonAssured.JsonPathAssertions> consumer) {
    return resp -> {
      var responseBody = resp.getResponseBody();
      if (responseBody == null) {
        throw new AssertionError("Response body is null");
      }
      try {
        consumer.accept(JsonAssured.assertJson(responseBody));
      } catch (Throwable e) {
        InternalUtils.rethrow(e);
      }
    };
  }
}

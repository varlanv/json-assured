package com.varlanv.jsonassured;

import java.util.function.Consumer;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

public interface JAWebFluxAdapter {

  static void tst() {
    WebTestClient.bindToServer()
        .build()
        .get()
        .uri("kek")
        .exchange()
        .expectBody()
        .consumeWith(
            jsonPathSpec(jsonSpec -> jsonSpec.stringPath("$.kek", s -> s.isEqualTo("kek"))));
  }

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

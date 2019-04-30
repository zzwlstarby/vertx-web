package io.vertx.ext.web.validation.testutils;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class TestRequest {

  WebClient client;
  HttpMethod method;
  String path;
  List<Consumer<HttpRequest<Buffer>>> requestTranformations;
  List<Consumer<HttpResponse<Buffer>>> responseAsserts;
  List<Condition<HttpResponse<Buffer>>> responseConditions;

  public TestRequest(WebClient client, HttpMethod method, String path) {
    this.client = client;
    this.method = method;
    this.path = path;
    this.requestTranformations = new ArrayList<>();
    this.responseAsserts = new ArrayList<>();
    this.responseConditions = new ArrayList<>();
  }

  public TestRequest withTransformations(Consumer<HttpRequest<Buffer>>... transformations) {
    requestTranformations.addAll(Arrays.asList(transformations));
    return this;
  }

  public TestRequest withResponseAsserts(Consumer<HttpResponse<Buffer>>... asserts) {
    responseAsserts.addAll(Arrays.asList(asserts));
    return this;
  }

  public TestRequest withResponseConditions(Condition<HttpResponse<Buffer>>... conditions) {
    responseConditions.addAll(Arrays.asList(conditions));
    return this;
  }

  public TestRequest send(VertxTestContext testContext, Checkpoint checkpoint) {
    HttpRequest<Buffer> req = client.request(method, path);
    this.requestTranformations.forEach(c -> c.accept(req));
    req.send(ar -> {
      if (ar.failed()) testContext.failNow(ar.cause());
      else {
        testContext.verify(() -> {
          this.responseConditions.forEach(c -> assertThat(ar.result()).satisfies(c));
          this.responseAsserts.forEach(c -> c.accept(ar.result()));
        });
        checkpoint.flag();
      }
    });
    return this;
  }

  public TestRequest sendJson(Object json, VertxTestContext testContext, Checkpoint checkpoint) {
    HttpRequest<Buffer> req = client.request(method, path);
    this.requestTranformations.forEach(c -> c.accept(req));
    req.sendJson(json, ar -> {
      if (ar.failed()) testContext.failNow(ar.cause());
      else {
        testContext.verify(() -> {
          this.responseConditions.forEach(c -> assertThat(ar.result()).satisfies(c));
          this.responseAsserts.forEach(c -> c.accept(ar.result()));
        });
        checkpoint.flag();
      }
    });
    return this;
  }

  public TestRequest sendURLEncodedForm(MultiMap form, VertxTestContext testContext, Checkpoint checkpoint) {
    HttpRequest<Buffer> req = client.request(method, path);
    this.requestTranformations.forEach(c -> c.accept(req));
    req.sendForm(form, ar -> {
      if (ar.failed()) testContext.failNow(ar.cause());
      else {
        testContext.verify(() -> {
          this.responseConditions.forEach(c -> assertThat(ar.result()).satisfies(c));
          this.responseAsserts.forEach(c -> c.accept(ar.result()));
        });
        checkpoint.flag();
      }
    });
    return this;
  }

  public TestRequest sendMultipartForm(MultipartForm form, VertxTestContext testContext, Checkpoint checkpoint) {
    HttpRequest<Buffer> req = client.request(method, path);
    this.requestTranformations.forEach(c -> c.accept(req));
    req.sendMultipartForm(form, ar -> {
      if (ar.failed()) testContext.failNow(ar.cause());
      else {
        testContext.verify(() -> {
          this.responseConditions.forEach(c -> assertThat(ar.result()).satisfies(c));
          this.responseAsserts.forEach(c -> c.accept(ar.result()));
        });
        checkpoint.flag();
      }
    });
    return this;
  }

  public static TestRequest testRequest(WebClient client, HttpMethod method, String path) {
    return new TestRequest(client, method, path);
  }

  public static Consumer<HttpResponse<Buffer>> statusCode(int statusCode) {
    return res -> assertThat(res.statusCode()).isEqualTo(statusCode);
  }

  public static Consumer<HttpResponse<Buffer>> statusMessage(String statusMessage) {
    return res -> assertThat(res.statusMessage()).isEqualTo(statusMessage);
  }

}

package io.vertx.ext.web.validation.testutils;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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

  public TestRequest(WebClient client, HttpMethod method, String path) {
    this.client = client;
    this.method = method;
    this.path = path;
    this.requestTranformations = new ArrayList<>();
    this.responseAsserts = new ArrayList<>();
  }

  public TestRequest transformations(Consumer<HttpRequest<Buffer>>... transformations) {
    requestTranformations.addAll(Arrays.asList(transformations));
    return this;
  }

  public TestRequest asserts(Consumer<HttpResponse<Buffer>>... asserts) {
    responseAsserts.addAll(Arrays.asList(asserts));
    return this;
  }

  public TestRequest send(VertxTestContext testContext, Checkpoint checkpoint) {
    HttpRequest<Buffer> req = client.request(method, path);
    this.requestTranformations.forEach(c -> c.accept(req));
    req.send(ar -> {
      if (ar.failed()) testContext.failNow(ar.cause());
      else {
        testContext.verify(() -> {
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

  public static TestRequest testRequest(WebClient client, HttpMethod method) {
    return new TestRequest(client, method, "/test");
  }

  public static Consumer<HttpResponse<Buffer>> statusCode(int statusCode) {
    return res -> assertThat(res.statusCode()).isEqualTo(statusCode);
  }

  public static Consumer<HttpResponse<Buffer>> statusMessage(String statusMessage) {
    return res -> assertThat(res.statusMessage()).isEqualTo(statusMessage);
  }

  public static Consumer<HttpRequest<Buffer>> header(String key, String value) {
    return req -> req.putHeader(key, value);
  }

  public static Consumer<HttpRequest<Buffer>> cookie(QueryStringEncoder encoder) {
    return req -> {
      try {
        String rawQuery = encoder.toUri().getRawQuery();
        if (rawQuery != null && !rawQuery.isEmpty())
          req.putHeader("cookie", encoder.toUri().getRawQuery());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    };
  }

  public static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }

}

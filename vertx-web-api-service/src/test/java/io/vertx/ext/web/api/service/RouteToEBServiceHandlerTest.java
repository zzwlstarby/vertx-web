package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.validation.BaseValidationHandlerTest;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;

import static io.vertx.ext.json.schema.generic.dsl.Schemas.*;
import static io.vertx.ext.web.validation.dsl.BodyProcessorFactory.json;
import static io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory.param;
import static io.vertx.ext.web.validation.testutils.TestRequest.*;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@SuppressWarnings("unchecked")
@ExtendWith(VertxExtension.class)
public class RouteToEBServiceHandlerTest extends BaseValidationHandlerTest {

  MessageConsumer<JsonObject> consumer;

  @AfterEach
  public void tearDown() {
    if (consumer != null) consumer.unregister();
  }

  @Test
  public void serviceProxyTypedTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    AnotherTestService service = new AnotherTestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(AnotherTestService.class, service);

    router
      .post("/testE/:id")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandler.builder(parser)
          .pathParameter(param("id", intSchema()))
          .body(json(objectSchema().property("value", intSchema())))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testE")
      );

    router
      .post("/testF/:id")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandler.builder(parser)
          .pathParameter(param("id", intSchema()))
          .body(json(
            anyOf(
              objectSchema().property("value", intSchema()),
              arraySchema().items(intSchema())
            )
          ))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testF")
      );


    testRequest(client, HttpMethod.POST, "/testE/123")
      .asserts(statusCode(200), statusMessage("OK"))
      .asserts(jsonBodyResponse(new JsonObject().put("id", 123).put("value", 1)))
      .sendJson(new JsonObject().put("value", 1), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testF/123")
      .asserts(statusCode(200), statusMessage("OK"))
      .asserts(jsonBodyResponse(new JsonArray().add(1 + 123).add(2 + 123).add(3 + 123)))
      .sendJson(new JsonArray().add(1).add(2).add(3), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testF/123")
      .asserts(statusCode(200), statusMessage("OK"))
      .asserts(jsonBodyResponse(new JsonObject().put("id", 123).put("value", 1)))
      .sendJson(new JsonObject().put("value", 1), testContext, checkpoint);
  }

  @Test
  public void serviceProxyDataObjectTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    AnotherTestService service = new AnotherTestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(AnotherTestService.class, service);

    router
      .post("/test")
      .handler(BodyHandler.create())
      .handler(
        ValidationHandler.builder(parser)
          .body(json(ref(JsonPointer.fromURI(URI.create("filter.json")))))
          .build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testDataObject")
      );

    FilterData data = FilterData.generate();

    JsonObject result = data.toJson().copy();
    result.remove("message");

    testRequest(client, HttpMethod.POST)
      .asserts(statusCode(200), statusMessage("OK"))
      .asserts(jsonBodyResponse(result))
      .sendJson(data.toJson(), testContext, checkpoint);
  }

  @Test
  public void emptyOperationResultTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(TestService.class, service);

    router
      .get("/test")
      .handler(
        ValidationHandler.builder(parser).build()
      ).handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testEmptyServiceResponse")
      );

    testRequest(client, HttpMethod.GET)
      .asserts(statusCode(200), statusMessage("OK"))
      .asserts(emptyResponse())
      .send(testContext, checkpoint);
  }

  private User fakeUser(String username) {
    return new User() {
      @Override public User isAuthorized(String s, Handler<AsyncResult<Boolean>> handler) {
        return null;
      }
      @Override public User clearCache() {
        return null;
      }
      @Override public JsonObject principal() {
        return new JsonObject().put("username", username);
      }
      @Override public void setAuthProvider(AuthProvider authProvider) { }
    };
  }

  @Test
  public void authorizedUserTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(TestService.class, service);

    router
      .get("/test")
      .handler(
        ValidationHandler.builder(parser).build()
      ).handler(rc -> {
        rc.setUser(fakeUser("slinkydeveloper")); // Put user mock into context
        rc.next();
      })
      .handler(
        RouteToEBServiceHandler.build(vertx.eventBus(), "someAddress", "testUser")
      );

    testRequest(client, HttpMethod.GET)
      .asserts(statusCode(200), statusMessage("OK"))
      .asserts(jsonBodyResponse(new JsonObject().put("result", "Hello slinkydeveloper!")))
      .send(testContext, checkpoint);
  }

  @Test
  public void extraPayloadTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint();

    TestService service = new TestServiceImpl(vertx);
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress("someAddress");
    consumer = serviceBinder.register(TestService.class, service);

    router
      .get("/test")
      .handler(
        ValidationHandler.builder(parser).build()
      ).handler(
        RouteToEBServiceHandler
          .build(vertx.eventBus(), "someAddress", "extraPayload")
          .extraPayloadMapper(rc -> new JsonObject().put("username", "slinkydeveloper"))
      );

    testRequest(client, HttpMethod.GET)
      .asserts(statusCode(200), statusMessage("OK"))
      .asserts(jsonBodyResponse(new JsonObject().put("result", "Hello slinkydeveloper!")))
      .send(testContext, checkpoint);
  }
}

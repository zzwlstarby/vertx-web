package io.vertx.ext.web.validation;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaParserOptions;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.ext.web.validation.dsl.ValidationHandlerBuilder;
import io.vertx.ext.web.validation.testutils.ValidationTestUtils;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.ext.json.schema.generic.dsl.Schemas.*;
import static io.vertx.ext.web.validation.dsl.BodyProcessorFactory.*;
import static io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory.param;
import static io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory.explodedParam;
import static io.vertx.ext.web.validation.testutils.TestRequest.*;
import static io.vertx.ext.web.validation.testutils.ValidationTestUtils.badBodyResponse;
import static io.vertx.ext.web.validation.testutils.ValidationTestUtils.badParameterResponse;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@SuppressWarnings("unchecked")
@ExtendWith(VertxExtension.class)
public class ValidationHandlerProcessorsIntegrationTest {

  SchemaRouter schemaRouter;
  SchemaParser parser;
  Router router;
  HttpServer server;
  WebClient client;

  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) {
    router = Router.router(vertx);
    ValidationTestUtils.mountRouterFailureHandler(router);

    schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(new SchemaParserOptions(), schemaRouter);

    client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(9000).setDefaultHost("localhost"));
    server = vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(9000, testContext.succeeding(h -> {
        testContext.completeNow();
      }));
  }

  @Test
  public void testPathParamsSimpleTypes(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .pathParameter(param("a", stringSchema()))
      .pathParameter(param("b", booleanSchema()))
      .pathParameter(param("c", intSchema()))
      .build();
    router.get("/testPathParams/:a/:b/:c")
      .handler(validationHandler)
      .handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusMessage(
          params.pathParameter("a").getString() + params.pathParameter("b").getBoolean() + params.pathParameter("c").getInteger()
        ).end();
    });
    String a = "hello";
    String b = "true";
    String c = "10";

    testRequest(client, HttpMethod.GET, String.format("/testPathParams/%s/%s/%s", a, b, c))
      .withResponseAsserts(statusCode(200), statusMessage(a + b + c))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testPathParams/hello/bla/10")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "b",
        ParameterLocation.PATH
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testQueryParamsSimpleTypes(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .queryParameter(param("param1", booleanSchema()))
      .queryParameter(param("param2", intSchema()))
      .build();
    router
      .get("/testQueryParams")
      .handler(validationHandler)
      .handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(
        params.queryParameter("param1").getBoolean().toString() + params.queryParameter("param2").getInteger().toString()
      ).end();
    });
    testRequest(client, HttpMethod.GET, "/testQueryParams?param1=true&param2=10")
      .withResponseAsserts(statusCode(200), statusMessage("true10"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testQueryParams?param1=true&param2=bla")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "param2",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }


  @Test
  public void testQueryArrayParamsArrayAndPathParam(VertxTestContext testContext) throws Exception {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .pathParameter(param("pathParam", booleanSchema()))
      .queryParameter(explodedParam("awesomeArray", arraySchema().items(intSchema())))
      .queryParameter(param("anotherParam", numberSchema()))
      .build();
    router
      .get("/testQueryParams/:pathParam")
      .handler(validationHandler)
      .handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext.response().setStatusMessage(
        params.pathParameter("pathParam").toString() +
          params.queryParameter("awesomeArray").toString() +
          params.queryParameter("anotherParam").toString()
      ).end();
    });

    testRequest(client, HttpMethod.GET, "/testQueryParams/true?awesomeArray=1&awesomeArray=2&awesomeArray=3&anotherParam=5.2")
      .withResponseAsserts(statusCode(200), statusMessage("true" + new JsonArray().add(1).add(2).add(3).toString() + "5.2"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testQueryParams/true?awesomeArray=1&awesomeArray=bla&awesomeArray=3&anotherParam=5.2")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "awesomeArray",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testFormURLEncoded(VertxTestContext testContext) throws Exception {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandler
      .builder(parser)
      .body(
        formUrlEncoded(objectSchema().requiredProperty("parameter", intSchema()))
      )
      .build();

    router.route().handler(BodyHandler.create());
    router
      .post("/testFormParam")
      .handler(validationHandler)
      .handler(routingContext -> {
      RequestParameters params = routingContext.get("parsedParameters");
      routingContext
        .response()
        .setStatusMessage(params.body().getJsonObject().getInteger("parameter").toString())
        .end();
    });

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200), statusMessage("5"))
      .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(
        badBodyResponse(BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR)
      )
      .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("parameter", "bla"), testContext, checkpoint);
  }

  @Test
  public void testMultipartForm(VertxTestContext testContext) throws Exception {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandler
      .builder(parser)
      .body(
        multipartFormData(objectSchema().requiredProperty("parameter", intSchema()))
      )
      .build();

    router.route().handler(BodyHandler.create());
    router
      .post("/testFormParam")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusMessage(params.body().getJsonObject().getInteger("parameter").toString())
          .end();
      });

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200), statusMessage("5"))
      .sendMultipartForm(MultipartForm.create().attribute("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(
        badBodyResponse(BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR)
      )
      .sendMultipartForm(MultipartForm.create().attribute("parameter", "bla"), testContext, checkpoint);
  }

  @Test
  public void testBothFormTypes(VertxTestContext testContext) throws Exception {
    Checkpoint checkpoint = testContext.checkpoint(6);

    ObjectSchemaBuilder bodySchema = objectSchema().requiredProperty("parameter", intSchema());

    ValidationHandler validationHandler = ValidationHandler
      .builder(parser)
      .body(multipartFormData(bodySchema))
      .body(formUrlEncoded(bodySchema))
      .build();

    router.route().handler(BodyHandler.create());
    router
      .post("/testFormParam")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        if (params.body() != null) {
          routingContext
            .response()
            .setStatusMessage(params.body().getJsonObject().getInteger("parameter").toString())
            .end();
        } else {
          routingContext
            .response()
            .setStatusMessage("No body")
            .end();
        }
      });

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200), statusMessage("5"))
      .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(
        badBodyResponse(BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR)
      )
      .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("parameter", "bla"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200), statusMessage("5"))
      .sendMultipartForm(MultipartForm.create().attribute("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(
        badBodyResponse(BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR)
      )
      .sendMultipartForm(MultipartForm.create().attribute("parameter", "bla"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200), statusMessage("No body"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(400))
      .withResponseAsserts(
        badBodyResponse(BodyProcessorException.BodyProcessorErrorType.MISSING_MATCHING_BODY_PROCESSOR)
      )
      .sendJson(new JsonObject(), testContext, checkpoint);
  }

  @Test
  public void testSameResultWithDifferentBodyTypes(VertxTestContext testContext) throws Exception {
    Checkpoint checkpoint = testContext.checkpoint(3);

    JsonObject expectedResult = new JsonObject()
      .put("int", 10)
      .put("string", "hello")
      .put("array", new JsonArray().add(1).add(1.1));

    ObjectSchemaBuilder bodySchema = objectSchema()
      .requiredProperty("int", intSchema())
      .requiredProperty("string", stringSchema())
      .property("array", arraySchema().items(numberSchema()));

    ValidationHandler validationHandler = ValidationHandler
      .builder(parser)
      .body(json(bodySchema))
      .body(multipartFormData(bodySchema))
      .body(formUrlEncoded(bodySchema))
      .build();

    router.route().handler(BodyHandler.create());
    router
      .post("/testFormParam")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        if (params.body().getJsonObject().equals(expectedResult)) {
          routingContext
            .response()
            .setStatusCode(200)
            .end();
        } else {
          routingContext
            .response()
            .setStatusCode(500)
            .end();
        }
      });

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200))
      .sendURLEncodedForm(
        MultiMap
          .caseInsensitiveMultiMap()
          .add("int", "10")
          .add("string", "hello")
          .add("array", "1")
          .add("array", "1.1"),
        testContext, checkpoint
      );

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200))
      .sendMultipartForm(
        MultipartForm.create()
          .attribute("int", "10")
          .attribute("string", "hello")
          .attribute("array", "1")
          .attribute("array", "1.1"),
        testContext, checkpoint
      );

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .withResponseAsserts(statusCode(200))
      .sendJson(expectedResult , testContext, checkpoint);
  }

  @Test
  public void testValidationHandlerChaining(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(1);

    ValidationHandler validationHandler1 = ValidationHandler
      .builder(parser)
      .queryParameter(param("param1", intSchema()))
      .build();

    ValidationHandler validationHandler2 = ValidationHandler
      .builder(parser)
      .queryParameter(param("param2", booleanSchema()))
      .build();

    router.get("/testHandlersChaining")
      .handler(validationHandler1)
      .handler(validationHandler2)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusMessage(
            params.queryParameter("param1").toString() +
              params.queryParameter("param2").toString()
          )
          .end();
    });

    testRequest(client, HttpMethod.GET, "/testHandlersChaining?param1=10&param2=true")
      .withResponseAsserts(statusCode(200), statusMessage("10true"))
      .send(testContext, checkpoint);
  }
}

package io.vertx.ext.web.validation;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
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
import io.vertx.ext.web.validation.dsl.ArrayParserFactory;
import io.vertx.ext.web.validation.dsl.ValidationHandlerBuilder;
import io.vertx.ext.web.validation.testutils.ValidationTestUtils;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.stream.Collectors;

import static io.vertx.ext.json.schema.generic.dsl.Keywords.multipleOf;
import static io.vertx.ext.json.schema.generic.dsl.Schemas.*;
import static io.vertx.ext.web.validation.dsl.BodyProcessorFactory.*;
import static io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory.optionalParam;
import static io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory.param;
import static io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory.*;
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
      .asserts(statusCode(200), statusMessage(a + b + c))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testPathParams/hello/bla/10")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
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
      .asserts(statusCode(200), statusMessage("true10"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testQueryParams?param1=true&param2=bla")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "param2",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testQueryJsonObjectAsyncParam(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .queryParameter(jsonParam("myTree", ref(JsonPointer.fromURI(URI.create("tree_schema.json")))))
      .build();
    router
      .get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .putHeader("content-type", "application/json")
          .end(params.queryParameter("myTree").getJsonObject().toBuffer());
      });

    JsonObject testSuccessObj = new JsonObject()
      .put("value", "aaa")
      .put("childs", new JsonArray().add(
        new JsonObject().put("value", "bbb")
      ));

    testRequest(client, HttpMethod.GET, "/test?myTree=" + urlEncode(testSuccessObj.encode()))
      .asserts(statusCode(200), jsonBodyResponse(testSuccessObj))
      .send(testContext, checkpoint);

    JsonObject testFailureObj = testSuccessObj.copy();
    testFailureObj.remove("value");

    testRequest(client, HttpMethod.GET, "/test?myTree=" + urlEncode(testFailureObj.encode()))
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR,
        "myTree",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testQueryParamsAsyncValidation(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .queryParameter(param("param1", booleanSchema()))
      .queryParameter(param("param2", ref(JsonPointer.fromURI(URI.create("int_schema.json"))), ValueParser.LONG_PARSER))
      .build();
    router
      .get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext.response().setStatusMessage(
          params.queryParameter("param1").getBoolean().toString() + params.queryParameter("param2").getInteger().toString()
        ).end();
      });
    testRequest(client, HttpMethod.GET, "/test?param1=true&param2=5")
      .asserts(statusCode(200), statusMessage("true5"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?param1=bla&param2=5")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "param1",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?param1=true&param2=bla")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "param2",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?param1=true&param2=15")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR,
        "param2",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testQueryParamOptional(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .queryParameter(param("param1", booleanSchema()))
      .queryParameter(optionalParam("param2", intSchema()))
      .build();
    router
      .get("/testQueryParams")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext.response().setStatusMessage(
          "" + params.queryParameter("param1") + params.queryParameter("param2")
        ).end();
      });

    testRequest(client, HttpMethod.GET, "/testQueryParams?param1=true&param2=10")
      .asserts(statusCode(200), statusMessage("true10"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testQueryParams?param1=true")
      .asserts(statusCode(200), statusMessage("truenull"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testQueryParams?param1=true&param2=bla")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "param2",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testQueryParamArrayExploded(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .queryParameter(explodedParam("parameter",
        arraySchema().items(intSchema().with(multipleOf(2)))
      ))
      .build();
    router
      .get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext.response().setStatusMessage(
          params.queryParameter("parameter").getJsonArray().stream().map(Object::toString).collect(Collectors.joining(","))
        ).end();
      });

    testRequest(client, HttpMethod.GET, "/test?parameter=2&parameter=4&parameter=6")
      .asserts(statusCode(200), statusMessage("2,4,6"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?parameter=2&parameter=2&parameter=false")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "parameter",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?parameter=2&parameter=2&parameter=1")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR,
        "parameter",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testQueryParamArrayCommaSeparated(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .queryParameter(serializedParam(
        "parameter",
        ArrayParserFactory.commaSeparatedArrayParser(),
        arraySchema().items(intSchema().with(multipleOf(2)))
      ))
      .build();
    router
      .get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext.response().setStatusMessage(
          params.queryParameter("parameter").getJsonArray().stream().map(Object::toString).collect(Collectors.joining(","))
        ).end();
      });

    testRequest(client, HttpMethod.GET, "/test?parameter=" + urlEncode("2,4,6"))
      .asserts(statusCode(200), statusMessage("2,4,6"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?parameter=" + urlEncode("1,false,3"))
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "parameter",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?parameter=" + urlEncode("6,2,1"))
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR,
        "parameter",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }


  @Test
  public void testQueryParamDefault(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .queryParameter(optionalParam("param1", intSchema().defaultValue(10)))
      .queryParameter(param("param2", intSchema()))
      .build();
    router
      .get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext.response().setStatusMessage(
          "" + params.queryParameter("param1") + params.queryParameter("param2")
        ).end();
      });

    testRequest(client, HttpMethod.GET, "/test?param1=5&param2=10")
      .asserts(statusCode(200), statusMessage("510"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?param2=10")
      .asserts(statusCode(200), statusMessage("1010"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?param1=5")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.MISSING_PARAMETER_WHEN_REQUIRED_ERROR,
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
      .asserts(statusCode(200), statusMessage("true" + new JsonArray().add(1).add(2).add(3).toString() + "5.2"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testQueryParams/true?awesomeArray=1&awesomeArray=bla&awesomeArray=3&anotherParam=5.2")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "awesomeArray",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testHeaderParamsSimpleTypes(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .headerParameter(param("x-a", stringSchema()))
      .headerParameter(param("x-b", booleanSchema()))
      .headerParameter(param("x-c", intSchema()))
      .build();
    router.get("/testHeaderParams")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusMessage(String
            .format("%s%s%s", params.headerParameter("x-a"), params.headerParameter("x-b"), params.headerParameter("x-c"))
          ).end();
      });
    String a = "hello";
    String b = "false";
    String c = "10";

    testRequest(client, HttpMethod.GET, "/testHeaderParams")
      .transformations(header("x-a", a), header("x-b", b), header("x-c", c))
      .asserts(statusCode(200), statusMessage(a + b + c))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/testHeaderParams")
      .transformations(header("x-a", a), header("x-b", "bla"), header("x-c", c))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "x-b",
        ParameterLocation.HEADER
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testHeaderParamsAsync(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .headerParameter(param("x-a", stringSchema()))
      .headerParameter(param("x-b", booleanSchema()))
      .headerParameter(param("x-c", ref(JsonPointer.fromURI(URI.create("int_schema.json"))), ValueParser.LONG_PARSER))
      .build();
    router.get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusMessage(String
            .format("%s%s%s", params.headerParameter("x-a"), params.headerParameter("x-b"), params.headerParameter("x-c"))
          ).end();
      });
    String a = "hello";
    String b = "false";
    String c = "10";

    testRequest(client, HttpMethod.GET)
      .transformations(header("x-a", a), header("x-b", b), header("x-c", c))
      .asserts(statusCode(200), statusMessage(a + b + c))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET)
      .transformations(header("x-a", a), header("x-b", "bla"), header("x-c", c))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "x-b",
        ParameterLocation.HEADER
      ))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET)
      .transformations(header("x-a", a), header("x-b", b), header("x-c", "bla"))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "x-c",
        ParameterLocation.HEADER
      ))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET)
      .transformations(header("x-a", a), header("x-b", b), header("x-c", "15"))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR,
        "x-c",
        ParameterLocation.HEADER
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testCookieParamsSimpleTypes(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .cookieParameter(param("param1", booleanSchema()))
      .cookieParameter(param("param2", intSchema()))
      .build();
    router
      .get("/testCookieParams")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusMessage(
            params.cookieParameter("param1").toString() + params.cookieParameter("param2").toString()
          )
          .end();
      });

    QueryStringEncoder successParams = new QueryStringEncoder("/");
    successParams.addParam("param1", "true");
    successParams.addParam("param2", "10");
    
    testRequest(client, HttpMethod.GET, "/testCookieParams")
      .transformations(cookie(successParams))
      .asserts(statusCode(200), statusMessage("true10"))
      .send(testContext, checkpoint);

    QueryStringEncoder failureParams = new QueryStringEncoder("/");
    failureParams.addParam("param1", "true");
    failureParams.addParam("param2", "bla");

    testRequest(client, HttpMethod.GET, "/testCookieParams")
      .transformations(cookie(failureParams))
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "param2",
        ParameterLocation.COOKIE
      ))
      .send(testContext, checkpoint);
  }

  @Test
  public void testCookieParamsAsync(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    ValidationHandler validationHandler = ValidationHandlerBuilder
      .create(parser)
      .cookieParameter(param("param1", booleanSchema()))
      .cookieParameter(param("param2", ref(JsonPointer.fromURI(URI.create("int_schema.json"))), ValueParser.LONG_PARSER))
      .build();
    router
      .get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusMessage(
            params.cookieParameter("param1").toString() + params.cookieParameter("param2").toString()
          )
          .end();
      });

    QueryStringEncoder successParams = new QueryStringEncoder("/");
    successParams.addParam("param1", "true");
    successParams.addParam("param2", "10");
    testRequest(client, HttpMethod.GET)
      .transformations(cookie(successParams))
      .asserts(statusCode(200), statusMessage("true10"))
      .send(testContext, checkpoint);

    QueryStringEncoder failureParams1 = new QueryStringEncoder("/");
    failureParams1.addParam("param1", "true");
    failureParams1.addParam("param2", "bla");
    testRequest(client, HttpMethod.GET)
      .transformations(cookie(failureParams1))
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "param2",
        ParameterLocation.COOKIE
      ))
      .send(testContext, checkpoint);

    QueryStringEncoder failureParams2 = new QueryStringEncoder("/");
    failureParams2.addParam("param1", "true");
    failureParams2.addParam("param2", "15");
    testRequest(client, HttpMethod.GET)
      .transformations(cookie(failureParams2))
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR,
        "param2",
        ParameterLocation.COOKIE
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
      .asserts(statusCode(200), statusMessage("5"))
      .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(400))
      .asserts(
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
      .asserts(statusCode(200), statusMessage("5"))
      .sendMultipartForm(MultipartForm.create().attribute("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(400))
      .asserts(
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
      .asserts(statusCode(200), statusMessage("5"))
      .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(400))
      .asserts(
        badBodyResponse(BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR)
      )
      .sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap().add("parameter", "bla"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(200), statusMessage("5"))
      .sendMultipartForm(MultipartForm.create().attribute("parameter", "5"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(400))
      .asserts(
        badBodyResponse(BodyProcessorException.BodyProcessorErrorType.PARSING_ERROR)
      )
      .sendMultipartForm(MultipartForm.create().attribute("parameter", "bla"), testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(200), statusMessage("No body"))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(400))
      .asserts(
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
      .asserts(statusCode(200))
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
      .asserts(statusCode(200))
      .sendMultipartForm(
        MultipartForm.create()
          .attribute("int", "10")
          .attribute("string", "hello")
          .attribute("array", "1")
          .attribute("array", "1.1"),
        testContext, checkpoint
      );

    testRequest(client, HttpMethod.POST, "/testFormParam")
      .asserts(statusCode(200))
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
      .asserts(statusCode(200), statusMessage("10true"))
      .send(testContext, checkpoint);
  }

  @Test
  public void testJsonBody(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler = ValidationHandler
      .builder(parser)
      .body(json(objectSchema()))
      .build();

    router.post("/test")
      .handler(BodyHandler.create())
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .setStatusMessage(
            params.body().getJsonObject().toString()
          )
          .end();
      });

    testRequest(client, HttpMethod.POST)
      .asserts(statusCode(200), statusMessage("{}"))
      .sendJson(new JsonObject(), testContext, checkpoint);

    testRequest(client, HttpMethod.POST)
      .asserts(statusCode(400))
      .asserts(badBodyResponse(BodyProcessorException.BodyProcessorErrorType.VALIDATION_ERROR))
      .sendJson("aaa", testContext, checkpoint);
  }

  @Test
  public void testJsonBodyAsyncCircular(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    ValidationHandler validationHandler1 = ValidationHandler
      .builder(parser)
      .body(json(ref(JsonPointer.fromURI(URI.create("tree_schema.json")))))
      .build();

    JsonObject testObj = new JsonObject()
      .put("value", "aaa")
      .put("childs", new JsonArray().add(
        new JsonObject().put("value", "bbb")
      ));

    router.post("/test")
      .handler(BodyHandler.create())
      .handler(validationHandler1)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .putHeader("content-type", "application/json")
          .end(params.body().getJsonObject().toBuffer());
      });

    testRequest(client, HttpMethod.POST)
      .asserts(statusCode(200), jsonBodyResponse(testObj))
      .sendJson(testObj, testContext, checkpoint);

    testRequest(client, HttpMethod.POST)
      .asserts(statusCode(400))
      .asserts(badBodyResponse(BodyProcessorException.BodyProcessorErrorType.VALIDATION_ERROR))
      .sendJson("aaa", testContext, checkpoint);
  }

  @Test
  public void testQueryExpandedObjectAdditionalPropertiesAndDefault(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);

    ValidationHandler validationHandler = ValidationHandler
      .builder(parser)
      .queryParameter(explodedParamOptional("explodedObject",
        objectSchema()
          .property("wellKnownProperty", intSchema())
          .additionalProperties(booleanSchema())
          .defaultValue(new JsonObject())
      ))
      .build();

    router.get("/test")
      .handler(validationHandler)
      .handler(routingContext -> {
        RequestParameters params = routingContext.get("parsedParameters");
        routingContext
          .response()
          .putHeader("content-type", "application/json")
          .end(params.queryParameter("explodedObject").getJsonObject().toBuffer());
      });

    testRequest(client, HttpMethod.GET)
      .asserts(statusCode(200), jsonBodyResponse(new JsonObject()))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?wellKnownProperty=10")
      .asserts(statusCode(200), jsonBodyResponse(new JsonObject().put("wellKnownProperty", 10)))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?wellKnownProperty=10&myFlag=false")
      .asserts(statusCode(200), jsonBodyResponse(new JsonObject().put("wellKnownProperty", 10).put("myFlag", false)))
      .send(testContext, checkpoint);

    testRequest(client, HttpMethod.GET, "/test?wellKnownProperty=10&myFlag=bla")
      .asserts(statusCode(400))
      .asserts(badParameterResponse(
        ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR,
        "explodedObject",
        ParameterLocation.QUERY
      ))
      .send(testContext, checkpoint);
  }

}

package io.vertx.ext.web.validation;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaParserOptions;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.validation.dsl.ValidationHandlerBuilder;
import io.vertx.ext.web.validation.testutils.ValidationTestUtils;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.ext.json.schema.generic.dsl.Schemas.*;
import static io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory.param;
import static io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory.explodedParam;
import static io.vertx.ext.web.validation.testutils.TestRequest.*;
import static io.vertx.ext.web.validation.testutils.ValidationTestUtils.badParameterResponse;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
@SuppressWarnings("unchecked")
@ExtendWith(VertxExtension.class)
public class ValidationHandlerIntegrationTest {

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
//
//  @Test
//  public void testQueryParamsArrayAndPathParamsFailureWithIncludedTypes() throws Exception {
//    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addPathParam("pathParam1",
//      ParameterType.INT).addQueryParamsArray("awesomeArray", ParameterType.EMAIL, true).addQueryParam("anotherParam",
//      ParameterType.DOUBLE, true);
//    schemaRouter.get("/testQueryParams/:pathParam1").handler(validationHandler);
//    schemaRouter.get("/testQueryParams/:pathParam1").handler(routingContext -> {
//      RequestParameters params = routingContext.get("parsedParameters");
//      routingContext.response().setStatusMessage(params.pathParameter("pathParam1").getInteger().toString() + params
//        .queryParameter("awesomeArray").getArray().size() + params.queryParameter("anotherParam").getDouble()
//        .toString()).end();
//    }).failureHandler(generateFailureHandler(true));
//
//    String pathParam = getSuccessSample(ParameterType.INT).getInteger().toString();
//    String arrayValue1 = getFailureSample(ParameterType.EMAIL);
//    String arrayValue2 = getSuccessSample(ParameterType.EMAIL).getString();
//    String anotherParam = getSuccessSample(ParameterType.DOUBLE).getDouble().toString();
//
//    QueryStringEncoder encoder = new QueryStringEncoder("/testQueryParams/" + URLEncoder.encode(pathParam, "UTF-8"));
//    encoder.addParam("awesomeArray", arrayValue1);
//    encoder.addParam("awesomeArray", arrayValue2);
//    encoder.addParam("anotherParam", anotherParam);
//
//    testRequest(HttpMethod.GET, encoder.toString(), 400, "failure:NO_MATCH");
//  }
//
//  @Test
//  public void testFormURLEncodedParamWithIncludedTypes() throws Exception {
//    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addFormParam("parameter",
//      ParameterType.INT, true);
//    schemaRouter.route().handler(BodyHandler.create());
//    schemaRouter.post("/testFormParam").handler(validationHandler);
//    schemaRouter.post("/testFormParam").handler(routingContext -> {
//      RequestParameters params = routingContext.get("parsedParameters");
//      routingContext.response().setStatusMessage(params.formParameter("parameter").getInteger().toString()).end();
//    }).failureHandler(generateFailureHandler(false));
//
//    String formParam = getSuccessSample(ParameterType.INT).getInteger().toString();
//
//    MultiMap form = MultiMap.caseInsensitiveMultiMap();
//    form.add("parameter", formParam);
//
//    testRequestWithForm(HttpMethod.POST, "/testFormParam", FormType.FORM_URLENCODED, form, 200, formParam);
//  }
//
//  @Test
//  public void testFormMultipartParamWithIncludedTypes() throws Exception {
//    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addFormParam("parameter",
//      ParameterType.INT, true);
//    schemaRouter.route().handler(BodyHandler.create());
//    schemaRouter.post("/testFormParam").handler(validationHandler);
//    schemaRouter.post("/testFormParam").handler(routingContext -> {
//      RequestParameters params = routingContext.get("parsedParameters");
//      routingContext.response().setStatusMessage(params.formParameter("parameter").getInteger().toString()).end();
//    }).failureHandler(generateFailureHandler(false));
//
//    String formParam = getSuccessSample(ParameterType.INT).getInteger().toString();
//
//    MultiMap form = MultiMap.caseInsensitiveMultiMap();
//    form.add("parameter", formParam);
//
//    testRequestWithForm(HttpMethod.POST, "/testFormParam", FormType.MULTIPART, form, 200, formParam);
//  }
//
//  @Test
//  public void testFormURLEncodedOverrideWithIncludedTypes() throws Exception {
//    HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addFormParam("parameter",
//      ParameterType.INT, true).addQueryParam("parameter", ParameterType.INT, true);
//    schemaRouter.route().handler(BodyHandler.create());
//    schemaRouter.post("/testFormParam").handler(validationHandler);
//    schemaRouter.post("/testFormParam").handler(routingContext -> {
//      RequestParameters params = routingContext.get("parsedParameters");
//      routingContext.response().setStatusMessage(params.formParameter("parameter").getInteger().toString()).end();
//    }).failureHandler(generateFailureHandler(false));
//
//    String formParam = getSuccessSample(ParameterType.INT).getInteger().toString();
//    String queryParam = getSuccessSample(ParameterType.INT).getInteger().toString();
//
//    MultiMap form = MultiMap.caseInsensitiveMultiMap();
//    form.add("parameter", formParam);
//
//    testRequestWithForm(HttpMethod.POST, "/testFormParam?parameter=" + queryParam, FormType.FORM_URLENCODED, form,
//      200, formParam);
//  }
//
//  @Test
//  public void testValidationHandlerChaining() throws Exception {
//    HTTPRequestValidationHandler validationHandler1 = HTTPRequestValidationHandler
//      .create()
//      .addQueryParam("param1", ParameterType.INT, true);
//    HTTPRequestValidationHandler validationHandler2 = HTTPRequestValidationHandler
//      .create()
//      .addQueryParam("param2", ParameterType.BOOL, true);
//    schemaRouter.route().handler(BodyHandler.create());
//    schemaRouter.get("/testHandlersChaining")
//      .handler(validationHandler1)
//      .handler(validationHandler2)
//      .handler(routingContext -> {
//        RequestParameters params = routingContext.get("parsedParameters");
//        assertNotNull(params.queryParameter("param1"));
//        assertNotNull(params.queryParameter("param2"));
//        routingContext
//          .response()
//          .setStatusMessage(
//            params.queryParameter("param1").getInteger().toString() +
//              params.queryParameter("param2").getBoolean()
//          ).end();
//    }).failureHandler(generateFailureHandler(false));
//
//    String param1 = getSuccessSample(ParameterType.INT).getInteger().toString();
//    String param2 = getSuccessSample(ParameterType.BOOL).getBoolean().toString();
//
//    testRequest(HttpMethod.GET, "/testHandlersChaining?param1=10&param2=true", 200, "10true");
//  }
}

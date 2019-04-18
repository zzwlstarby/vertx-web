package io.vertx.ext.web.validation.testutils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;

import java.util.regex.Pattern;

import static io.vertx.ext.json.schema.draft7.dsl.Schemas.*;

public class TestSchemas {

  public static ObjectSchemaBuilder SAMPLE_OBJECT_SCHEMA_BUILDER =
    objectSchema()
      .property("someNumbers", arraySchema().items(numberSchema()))
      .property("oneNumber", numberSchema())
      .patternProperty(Pattern.compile("someIntegers"), arraySchema().items(intSchema()))
      .patternProperty(Pattern.compile("oneInteger"), intSchema())
      .additionalProperties(booleanSchema());

  public static JsonObject VALID_OBJECT =
    new JsonObject()
      .put("someNumbers", new JsonArray().add(1.1).add(2.2))
      .put("oneNumber", 3.3)
      .put("someIntegers", new JsonArray().add(1).add(2))
      .put("oneInteger", 3)
      .put("aBoolean", true);

  public static JsonObject INVALID_OBJECT =
    new JsonObject()
      .put("someNumbers", new JsonArray().add(1.1).add(2.2))
      .put("oneNumber", 3.3)
      .put("someIntegers", new JsonArray().add(1).add(2))
      .put("oneInteger", 3)
      .put("aBoolean", "bla");

}

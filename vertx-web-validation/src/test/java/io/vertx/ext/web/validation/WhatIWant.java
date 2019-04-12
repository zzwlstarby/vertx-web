package io.vertx.ext.web.validation;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.validation.dsl.ArrayParserFactory;
import org.junit.Test;

import static io.vertx.ext.json.schema.draft7.dsl.Keywords.maximum;
import static io.vertx.ext.json.schema.draft7.dsl.Schemas.*;
import static io.vertx.ext.web.validation.dsl.BodyProcessorFactory.json;
import static io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory.param;
import static io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory.explodedParam;
import static io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory.serializedParam;

public class WhatIWant {

  @Test
  public void example() {
    SchemaParser parser = null;

    ValidationHandler h = ValidationHandler
      .builder(parser)
      .pathParameter(
        param("myInt", intSchema())
      )
      .queryParameter(
        serializedParam(
          "myArr",
          ArrayParserFactory.commaSeparatedArrayParser(),
          arraySchema()
            .items(intSchema())
        )
      )
      .queryParameter(
        explodedParam("myObj",
          objectSchema()
            .additionalProperties(stringSchema())
        )
      )
      .body(json(
        objectSchema()
          .property("a", intSchema().with(maximum(20)))
          .property("b", refToAlias("otherSchema"))
      ))
      .build();
  }

}

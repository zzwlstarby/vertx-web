package io.vertx.ext.web.validation;

import io.vertx.ext.json.schema.SchemaParser;
import org.junit.Test;

import static io.vertx.ext.json.schema.draft7.dsl.Keywords.*;
import static io.vertx.ext.json.schema.draft7.dsl.Schemas.*;
import static io.vertx.ext.web.validation.BodyProcessor.*;
import static io.vertx.ext.web.validation.SimpleParameterProcessor.*;
import static io.vertx.ext.web.validation.StyledParameterProcessor.*;

public class WhatIWant {

  @Test
  public void example1() {
    SchemaParser parser = null;

    // compile time checked with builder types
    // horrible to see parser passed every time as constructor param of schema builder
    // requires change in schema builder apis

    ValidationHandler.create()
      .pathParameter(
        param("myInt", intSchema(parser))
      )
      .queryParameter(
        serializedParam(
          "myArr",
          ArrayParserFactory.commaSeparatedArrayParser(),
          arraySchema(parser)
            .items(intSchema(parser))
        )
      )
      .queryParameter(
        explodedParam("myObj",
          objectSchema(parser)
            .additionalProperties(stringSchema(parser))
        )
      )
      .body(json(
        objectSchema()
          .property("a", intSchema(parser).with(maximum(20)))
          .property("b", refToAlias(parser, "otherSchema"))
      ));
  }

  @Test
  public void example2() {
    SchemaParser parser = null;

    // Runtime checked with IAE thrown when type is missing, one single method param(String,Schema)
    // Type inferred from schema (can share same algo for openapi stuff)

    ValidationHandler.create()
      .pathParameter(
        param("myInt", intSchema().build(parser))
      )
      .queryParameter(
        serializedParam(
          "myArr",
          ArrayParserFactory.commaSeparatedArrayParser(),
          arraySchema()
            .items(intSchema())
            .build(parser)
        )
      )
      .queryParameter(
        explodedParam("myObj",
          objectSchema()
            .additionalProperties(stringSchema())
            .build(parser)
        )
      )
      .body(json(
        objectSchema()
          .property("a", intSchema().with(maximum(20)))
          .property("b", refToAlias("otherSchema"))
          .build(parser)
      ));
  }

  @Test
  public void example3() {
    SchemaParser parser = null;

    // Coupling between validation handler and vertx json schema lib

    ValidationHandler
      .create(parser)
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
      ));
  }

}

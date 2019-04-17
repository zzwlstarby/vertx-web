package io.vertx.ext.web.validation.dsl.impl;

import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.dsl.ArrayItemByItemParserFactory;
import io.vertx.ext.web.validation.dsl.ArrayParserFactory;
import io.vertx.ext.web.validation.dsl.ObjectParserFactory;
import io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory;
import io.vertx.ext.web.validation.impl.ParameterProcessorImpl;
import io.vertx.ext.web.validation.impl.SchemaValidator;
import io.vertx.ext.web.validation.impl.SingleValueParameterParser;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;

public class ValidationDSLUtils {

  public static SimpleParameterProcessorFactory createArrayParamFactory(String parameterName, ArrayItemByItemParserFactory arrayItemByItemParserFactory, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ValueParser parser = (schemaBuilder.isItemByItemArraySchema()) ?
        arrayItemByItemParserFactory.newArrayParser(
          ValueParserInferenceUtils.infeerItemByItemParsersForArraySchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(s.getJson())
        ) :
        arrayParserFactory.newArrayParser(
          ValueParserInferenceUtils.infeerItemsParserForArraySchema(s.getJson())
        );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        true,
        new SingleValueParameterParser(parameterName, parser),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static SimpleParameterProcessorFactory createObjectParamFactory(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ValueParser parser =
        objectParserFactory.newArrayParser(
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s.getJson())
        );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new SingleValueParameterParser(parameterName, parser),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }
}

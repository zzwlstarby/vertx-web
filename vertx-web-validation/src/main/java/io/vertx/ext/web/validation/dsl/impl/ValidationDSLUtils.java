package io.vertx.ext.web.validation.dsl.impl;

import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterParser;
import io.vertx.ext.web.validation.ParameterProcessor;
import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.dsl.ArrayParserFactory;
import io.vertx.ext.web.validation.dsl.ObjectParserFactory;
import io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory;
import io.vertx.ext.web.validation.impl.*;

import java.util.function.BiFunction;

public class ValidationDSLUtils {

  public static BiFunction<ParameterLocation, SchemaParser, ParameterProcessor> createArrayParamFactory(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ValueParser parser = (schemaBuilder.isItemByItemArraySchema()) ?
        arrayParserFactory.newTupleParser(
          ValueParserInferenceUtils.infeerTupleParsersForArraySchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(s.getJson())
        ) :
        arrayParserFactory.newArrayParser(
          ValueParserInferenceUtils.infeerItemsParserForArraySchema(s.getJson())
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

  public static BiFunction<ParameterLocation, SchemaParser, ParameterProcessor> createObjectParamFactory(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ValueParser parser =
        objectParserFactory.newObjectParser(
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

  public static StyledParameterProcessorFactory createExplodedArrayParamFactory(String parameterName, ArraySchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      ParameterParser parser = (schemaBuilder.isItemByItemArraySchema()) ?
        new ExplodedTupleValueParameterParser(
          ValueParserInferenceUtils.infeerTupleParsersForArraySchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(s.getJson()),
          parameterName
        ) :
        new ExplodedArrayValueParameterParser(
          ValueParserInferenceUtils.infeerItemsParserForArraySchema(s.getJson()),
          parameterName
        );
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        parser,
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static StyledParameterProcessorFactory createExplodedObjectParamFactory(String parameterName, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new ExplodedObjectValueParameterParser(
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s.getJson()),
          parameterName
        ),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }

  public static StyledParameterProcessorFactory createDeepObjectParamFactory(String parameterName, ObjectSchemaBuilder schemaBuilder, boolean isOptional) {
    return (location, jsonSchemaParser) -> {
      Schema s = schemaBuilder.build(jsonSchemaParser);
      return new ParameterProcessorImpl(
        parameterName,
        location,
        isOptional,
        new DeepObjectValueParameterParser(
          ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s.getJson()),
          ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s.getJson()),
          parameterName
        ),
        new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
      );
    };
  }
}

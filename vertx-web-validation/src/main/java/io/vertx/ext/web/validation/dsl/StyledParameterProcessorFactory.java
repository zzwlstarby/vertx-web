package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.SchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.TupleSchemaBuilder;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterProcessor;
import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.dsl.impl.ValidationDSLUtils;
import io.vertx.ext.web.validation.impl.ParameterProcessorImpl;
import io.vertx.ext.web.validation.impl.SchemaValidator;
import io.vertx.ext.web.validation.impl.SingleValueParameterParser;

@FunctionalInterface
public interface StyledParameterProcessorFactory {

  ParameterProcessor create(ParameterLocation location, SchemaParser parser);

  static StyledParameterProcessorFactory jsonParam(String parameterName, SchemaBuilder builder) {
    return (location, parser) -> new ParameterProcessorImpl(
      parameterName,
      location,
      false,
      new SingleValueParameterParser(parameterName, ValueParser.JSON_PARSER),
      new SchemaValidator(builder.build(parser))
    );
  }

  static StyledParameterProcessorFactory jsonParamOptional(String parameterName, SchemaBuilder builder) {
    return (location, parser) -> new ParameterProcessorImpl(
      parameterName,
      location,
      true,
      new SingleValueParameterParser(parameterName, ValueParser.JSON_PARSER),
      new SchemaValidator(builder.build(parser))
    );
  }

  static StyledParameterProcessorFactory serializedParam(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createArrayParamFactory(parameterName, arrayParserFactory, schemaBuilder, false)::apply;
  }

  static StyledParameterProcessorFactory serializedParamOptional(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createArrayParamFactory(parameterName, arrayParserFactory, schemaBuilder, true)::apply;
  }

  static StyledParameterProcessorFactory serializedParam(String parameterName, TupleParserFactory tupleParserFactory, TupleSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createTupleParamFactory(parameterName, tupleParserFactory, schemaBuilder, false)::apply;
  }

  static StyledParameterProcessorFactory serializedParamOptional(String parameterName, TupleParserFactory tupleParserFactory, TupleSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createTupleParamFactory(parameterName, tupleParserFactory, schemaBuilder, true)::apply;
  }

  static StyledParameterProcessorFactory serializedParam(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createObjectParamFactory(parameterName, objectParserFactory, schemaBuilder, false)::apply;
  }

  static StyledParameterProcessorFactory serializedParamOptional(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createObjectParamFactory(parameterName, objectParserFactory, schemaBuilder, true)::apply;
  }

  static StyledParameterProcessorFactory explodedParam(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createExplodedArrayParamFactory(parameterName, schemaBuilder, false);
  }

  static StyledParameterProcessorFactory explodedParamOptional(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createExplodedArrayParamFactory(parameterName, schemaBuilder, true);
  }

  static StyledParameterProcessorFactory explodedParam(String parameterName, TupleSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createExplodedTupleParamFactory(parameterName, schemaBuilder, false);
  }

  static StyledParameterProcessorFactory explodedParamOptional(String parameterName, TupleSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createExplodedTupleParamFactory(parameterName, schemaBuilder, true);
  }

  static StyledParameterProcessorFactory explodedParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createExplodedObjectParamFactory(parameterName, schemaBuilder, false);
  }

  static StyledParameterProcessorFactory explodedParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createExplodedObjectParamFactory(parameterName, schemaBuilder, true);
  }

  static StyledParameterProcessorFactory deepObjectParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createDeepObjectParamFactory(parameterName, schemaBuilder, false);
  }

  static StyledParameterProcessorFactory deepObjectParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createDeepObjectParamFactory(parameterName, schemaBuilder, true);
  }

}

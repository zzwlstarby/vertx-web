package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.SchemaBuilder;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterProcessor;

@FunctionalInterface
public interface StyledParameterProcessorFactory {

  ParameterProcessor create(ParameterLocation location, SchemaParser parser);

  static StyledParameterProcessorFactory jsonParam(String parameterName, SchemaBuilder builder) {
    return null;
  }

  static StyledParameterProcessorFactory serializedParam(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory serializedParamOptional(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory serializedParam(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory serializedParamOptional(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory explodedParam(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory explodedParamOptional(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory explodedParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory explodedParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory deepObjectParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessorFactory deepObjectParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

}

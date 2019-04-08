package io.vertx.ext.web.validation;

import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.SchemaBuilder;

public interface StyledParameterProcessor extends ParameterProcessor {

  //TODO this:

  static StyledParameterProcessor jsonParam(String parameterName, SchemaBuilder builder) {
    return null;
  }

  static StyledParameterProcessor serializedArrayParam(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor serializedArrayParamOptional(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor serializedObjectParam(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor serializedObjectParamOptional(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedArrayParam(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedArrayParamOptional(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedObjectParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedObjectParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor deepObjectParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor deepObjectParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  //TODO or:

  static StyledParameterProcessor jsonParam(String parameterName, SchemaBuilder builder) {
    return null;
  }

  static StyledParameterProcessor serializedParam(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor serializedParamOptional(String parameterName, ArrayParserFactory arrayParserFactory, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor serializedParam(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor serializedParamOptional(String parameterName, ObjectParserFactory objectParserFactory, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedParam(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedParamOptional(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor explodedParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor deepObjectParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static StyledParameterProcessor deepObjectParamOptional(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

}

package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.NumberSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.StringSchemaBuilder;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterProcessor;
import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.dsl.impl.ValidationDSLUtils;
import io.vertx.ext.web.validation.impl.ParameterProcessorImpl;
import io.vertx.ext.web.validation.impl.SchemaValidator;
import io.vertx.ext.web.validation.impl.SingleValueParameterParser;

@FunctionalInterface
public interface SimpleParameterProcessorFactory {

  ParameterProcessor create(ParameterLocation location, SchemaParser jsonSchemaParser);

  static SimpleParameterProcessorFactory param(String parameterName, NumberSchemaBuilder schemaBuilder) {
    return (location, jsonSchemaParser) -> new ParameterProcessorImpl(
      parameterName,
      location,
      false,
      new SingleValueParameterParser(parameterName, schemaBuilder.isIntegerSchema() ? ValueParser.LONG_PARSER : ValueParser.DOUBLE_PARSER),
      new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
    );
  }

  static SimpleParameterProcessorFactory optionalParam(String parameterName, NumberSchemaBuilder schemaBuilder) {
    return (location, jsonSchemaParser) -> new ParameterProcessorImpl(
      parameterName,
      location,
      true,
      new SingleValueParameterParser(parameterName, schemaBuilder.isIntegerSchema() ? ValueParser.LONG_PARSER : ValueParser.DOUBLE_PARSER),
      new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
    );
  }

  static SimpleParameterProcessorFactory param(String parameterName, StringSchemaBuilder schemaBuilder) {
    return (location, jsonSchemaParser) -> new ParameterProcessorImpl(
      parameterName,
      location,
      false,
      new SingleValueParameterParser(parameterName, ValueParser.NOOP_PARSER),
      new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
    );
  }

  static SimpleParameterProcessorFactory optionalParam(String parameterName, StringSchemaBuilder schemaBuilder) {
    return (location, jsonSchemaParser) -> new ParameterProcessorImpl(
      parameterName,
      location,
      true,
      new SingleValueParameterParser(parameterName, ValueParser.NOOP_PARSER),
      new SchemaValidator(schemaBuilder.build(jsonSchemaParser))
    );
  }

  static SimpleParameterProcessorFactory param(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createArrayParamFactory(
      parameterName,
      ArrayItemByItemParserFactory.commaSeparatedArrayParser(),
      ArrayParserFactory.commaSeparatedArrayParser(),
      schemaBuilder,
      false
    );
  }

  static SimpleParameterProcessorFactory optionalParam(String parameterName, ArraySchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createArrayParamFactory(
      parameterName,
      ArrayItemByItemParserFactory.commaSeparatedArrayParser(),
      ArrayParserFactory.commaSeparatedArrayParser(),
      schemaBuilder,
      true
    );
  }

  static SimpleParameterProcessorFactory param(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createObjectParamFactory(
      parameterName,
      ObjectParserFactory.commaSeparatedObjectParser(),
      schemaBuilder,
      false
    );
  }

  static SimpleParameterProcessorFactory optionalParam(String parameterName, ObjectSchemaBuilder schemaBuilder) {
    return ValidationDSLUtils.createObjectParamFactory(
      parameterName,
      ObjectParserFactory.commaSeparatedObjectParser(),
      schemaBuilder,
      true
    );
  }

}

package io.vertx.ext.web.validation.dsl.impl;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterProcessor;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.dsl.BodyProcessorFactory;
import io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory;
import io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory;
import io.vertx.ext.web.validation.dsl.ValidationHandlerBuilder;

public class ValidationHandlerBuilderImpl implements ValidationHandlerBuilder {

  SchemaParser jsonSchemaParser;

  public ValidationHandlerBuilderImpl(SchemaParser jsonSchemaParser) {
    this.jsonSchemaParser = jsonSchemaParser;
  }

  @Override
  public ValidationHandlerBuilder parameter(ParameterLocation location, ParameterProcessor processor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder queryParameter(StyledParameterProcessorFactory parameterProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder queryParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder pathParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder cookieParameter(StyledParameterProcessorFactory parameterProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder cookieParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder headerParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder body(BodyProcessorFactory bodyProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder predicate(RequestPredicate predicate) {
    return null;
  }

  @Override
  public ValidationHandler build() {
    return null;
  }
}

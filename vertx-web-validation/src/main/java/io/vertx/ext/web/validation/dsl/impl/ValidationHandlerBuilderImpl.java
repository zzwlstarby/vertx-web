package io.vertx.ext.web.validation.dsl.impl;

import io.vertx.ext.web.validation.BodyProcessor;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterProcessor;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory;
import io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory;
import io.vertx.ext.web.validation.dsl.ValidationHandlerBuilder;

public class ValidationHandlerBuilderImpl implements ValidationHandlerBuilder {
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
  public ValidationHandlerBuilder body(BodyProcessor bodyProcessor) {
    return null;
  }

  @Override
  public ValidationHandlerBuilder bodyRequired(boolean bodyRequired) {
    return null;
  }

  @Override
  public ValidationHandler build() {
    return null;
  }
}

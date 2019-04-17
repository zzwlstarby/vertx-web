package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.BodyProcessor;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.Validator;

public class TextPlainBodyProcessorImpl implements BodyProcessor {

  Validator valueValidator;

  public TextPlainBodyProcessorImpl(Validator valueValidator) {
    this.valueValidator = valueValidator;
  }

  @Override
  public boolean canProcess(String contentType) {
    return contentType.contains("text/plain");
  }

  @Override
  public Future<RequestParameter> process(RoutingContext requestContext) {
    return valueValidator.validate(requestContext.getBodyAsString());
  }
}

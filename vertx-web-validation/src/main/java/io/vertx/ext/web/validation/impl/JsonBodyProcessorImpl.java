package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.BodyProcessor;
import io.vertx.ext.web.validation.RequestParameter;

public class JsonBodyProcessorImpl implements BodyProcessor {
  @Override
  public boolean canProcess(String contentType) {
    return false;
  }

  @Override
  public Future<RequestParameter> process(RoutingContext requestContext) {
    return null;
  }
}

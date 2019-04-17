package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.BodyProcessor;
import io.vertx.ext.web.validation.RequestParameter;

public class FormBodyProcessorImpl implements BodyProcessor {

  private String contentType;

  public FormBodyProcessorImpl(String contentType) {
    this.contentType = contentType;
  }

  @Override
  public boolean canProcess(String contentType) {
    return contentType.contains(this.contentType);
  }

  @Override
  public Future<RequestParameter> process(RoutingContext requestContext) {
    MultiMap multiMap = requestContext.request().formAttributes();
    //TODO exploded form attributes?!?!?!
  }

}

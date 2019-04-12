package io.vertx.ext.web.validation;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

public interface BodyProcessor {

  boolean canProcess(String contentType);

  Future<RequestParameter> process(RoutingContext requestContext);

}

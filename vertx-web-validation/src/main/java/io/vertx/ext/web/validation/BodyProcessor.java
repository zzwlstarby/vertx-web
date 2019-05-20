package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

@VertxGen
public interface BodyProcessor {

  boolean canProcess(String contentType);

  Future<RequestParameter> process(RoutingContext requestContext);

}

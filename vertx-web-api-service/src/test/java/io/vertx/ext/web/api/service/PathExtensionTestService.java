package io.vertx.ext.web.api.service;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@WebApiServiceGen
@VertxGen
public interface PathExtensionTestService {
  void pathLevelGet(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void getPathLevel(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void pathLevelPost(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);

  void postPathLevel(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler);
}

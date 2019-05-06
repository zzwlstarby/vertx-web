package io.vertx.ext.web.api.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class PathExtensionTestServiceImpl implements PathExtensionTestService {
  @Override
  public void pathLevelGet(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusMessage("pathLevelGet")));
  }

  @Override
  public void getPathLevel(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusMessage("getPathLevel")));
  }

  @Override
  public void pathLevelPost(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusMessage("pathLevelPost")));
  }

  @Override
  public void postPathLevel(ServiceRequest context, Handler<AsyncResult<ServiceResponse>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(new ServiceResponse().setStatusMessage("postPathLevel")));
  }
}

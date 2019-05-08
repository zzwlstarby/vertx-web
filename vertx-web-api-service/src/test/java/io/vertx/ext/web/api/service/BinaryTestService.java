package io.vertx.ext.web.api.service;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@WebApiServiceGen
@VertxGen
public interface BinaryTestService {

  void binaryTest(
    final ServiceRequest request,
    final Handler<AsyncResult<ServiceResponse>> response);

}

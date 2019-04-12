package io.vertx.ext.web.validation;

import io.vertx.core.Future;

@FunctionalInterface
public interface Validator {

  Future<RequestParameter> validate(Object json);

}

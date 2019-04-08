package io.vertx.ext.web.validation;

import io.vertx.core.Future;

@FunctionalInterface
public interface Validator<T> {

  Future<RequestParameter> validate(T value);

}

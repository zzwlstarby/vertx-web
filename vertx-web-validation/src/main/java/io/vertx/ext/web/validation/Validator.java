package io.vertx.ext.web.validation;

import io.vertx.core.Future;

public interface Validator {

  Future<RequestParameter> validate(Object json);

  Object getDefault();

}

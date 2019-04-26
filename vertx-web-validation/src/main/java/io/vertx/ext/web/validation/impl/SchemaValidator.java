package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.ValidationException;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.Validator;

public class SchemaValidator implements Validator {

  Schema s;

  public SchemaValidator(Schema s) {
    this.s = s;
  }

  @Override
  public Future<RequestParameter> validate(Object json) {
    if (s.isSync()) {
      try {
        s.validateSync(json);
        s.applyDefaultValues(json);
        return Future.succeededFuture(RequestParameter.create(json));
      } catch (ValidationException e) {
        return Future.failedFuture(e);
      }
    }
    return s.validateAsync(json).map(v -> {
      s.applyDefaultValues(json);
      return RequestParameter.create(json);
    });
  }
}

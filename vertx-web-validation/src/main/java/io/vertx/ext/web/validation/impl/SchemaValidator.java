package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.Validator;

public class SchemaValidator implements Validator {

  Schema s;

  public SchemaValidator(Schema s) {
    this.s = s;
  }

  @Override
  public Future<RequestParameter> validate(Object json) {
    return s.validateAsync(json).map(v -> {
      s.applyDefaultValues(json);
      return RequestParameter.create(json);
    });
  }
}

package io.vertx.ext.web.validation;

import io.vertx.core.Future;
import io.vertx.ext.json.schema.Schema;

import java.util.Map;

public interface ParameterProcessor {

  boolean canProcess(Map<String, String> params);

  Future<RequestParameter> process(Map<String, String> params);

  static <T> ParameterProcessor create(String parameterName, ParameterParser<T> parser, Validator<T> validator, boolean isOptional) {
    return null;
  }

}

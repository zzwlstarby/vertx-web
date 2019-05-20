package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.ext.web.validation.impl.ParameterProcessorImpl;

import java.util.List;
import java.util.Map;

@VertxGen
public interface ParameterProcessor {

  @GenIgnore //TODO fix
  Future<RequestParameter> process(Map<String, List<String>> params);

  String getName();

  ParameterLocation getLocation();

  @GenIgnore
  static ParameterProcessor create(String parameterName, ParameterLocation location, boolean isOptional, ParameterParser parser, Validator validator) {
    return new ParameterProcessorImpl(parameterName, location, isOptional, parser, validator);
  }
}

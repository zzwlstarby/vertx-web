package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.ext.web.validation.*;

import java.util.List;
import java.util.Map;

import static io.vertx.ext.web.validation.ParameterProcessorException.*;

public class ParameterProcessorImpl implements ParameterProcessor {

  private String parameterName;
  private ParameterLocation location;
  private boolean isOptional;
  private ParameterParser parser;
  private Validator validator;

  public ParameterProcessorImpl(String parameterName, ParameterLocation location, boolean isOptional, ParameterParser parser, Validator validator) {
    this.parameterName = parameterName;
    this.location = location;
    this.isOptional = isOptional;
    this.parser = parser;
    this.validator = validator;
  }

  @Override
  public Future<RequestParameter> process(Map<String, List<String>> params) {
    Object json;
    try {
      json = parser.parseParameter(params);
    } catch (MalformedValueException e) {
      return Future.failedFuture(createParsingError(parameterName, location, e));
    }
    if (json != null)
      return validator.validate(json).recover(t -> Future.failedFuture(createValidationError(parameterName, location, t)));
    else if (!isOptional)
      return Future.failedFuture(createMissingParameterWhenRequired(parameterName, location));
    else
      return Future.succeededFuture();

  }
}

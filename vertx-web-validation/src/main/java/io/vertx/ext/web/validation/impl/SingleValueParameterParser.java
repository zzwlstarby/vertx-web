package io.vertx.ext.web.validation.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ParameterParser;
import io.vertx.ext.web.validation.ValueParser;

import java.util.List;
import java.util.Map;

public class SingleValueParameterParser implements ParameterParser  {

  String parameterName;
  ValueParser valueParser;

  public SingleValueParameterParser(String parameterName, ValueParser valueParser) {
    this.parameterName = parameterName;
    this.valueParser = valueParser;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameterValue) throws MalformedValueException {
    String extracted = parameterValue.get(parameterName).get(0);
    return extracted != null ? valueParser.parse(extracted) : null;
  }
}

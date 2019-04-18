package io.vertx.ext.web.validation.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ParameterParser;
import io.vertx.ext.web.validation.ValueParser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

public class ExplodedArrayValueParameterParser extends ArrayParser<String> implements ParameterParser {

  String parameterName;

  public ExplodedArrayValueParameterParser(ValueParser<String> itemsParser, String parameterName) {
    super(itemsParser);
    this.parameterName = parameterName;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameters) throws MalformedValueException {
    return parameters.containsKey(parameterName) ? parameters
      .remove(parameterName)
      .stream()
      .map(this::parseValue)
      .collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::addAll)) : null;
  }

  @Override
  protected boolean isSerializedEmpty(String serialized) {
    return serialized.isEmpty();
  }
}

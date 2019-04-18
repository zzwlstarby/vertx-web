package io.vertx.ext.web.validation.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ParameterParser;
import io.vertx.ext.web.validation.ValueParser;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DeepObjectValueParameterParser extends ObjectParser<String> implements ParameterParser {

  String parameterName;

  public DeepObjectValueParameterParser(Map<String, ValueParser<String>> propertiesParsers, Map<Pattern, ValueParser<String>> patternPropertiesParsers, ValueParser<String> additionalPropertiesParsers, String parameterName) {
    super(propertiesParsers, patternPropertiesParsers, additionalPropertiesParsers);
    this.parameterName = parameterName;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameters) throws MalformedValueException {
    JsonObject obj = new JsonObject();
    for (String key: parameters.keySet()) {
      if (key.contains(parameterName + "[") && key.charAt(key.length() - 1) == ']') {
        String realParameterName = key.substring(parameterName.length() + 1, key.length() - 1);
        Object parsed = parseField(realParameterName, parameters.remove(key).get(0));
        if (parsed != null) obj.put(realParameterName, parsed);
      }
    }
    return obj.isEmpty() ? null : obj;
  }

  @Override
  protected boolean isSerializedEmpty(String serialized) {
    return serialized.isEmpty();
  }
}

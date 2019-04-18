package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ValueParser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SplitterCharObjectParser extends ObjectParser<String> implements ValueParser<String> {

  private String separator;

  public SplitterCharObjectParser(Map<String, ValueParser<String>> propertiesParsers, Map<Pattern, ValueParser<String>> patternPropertiesParsers, ValueParser<String> additionalPropertiesParsers, String separator) {
    super(propertiesParsers, patternPropertiesParsers, additionalPropertiesParsers);
    this.separator = separator;
  }

  @Override
  public JsonObject parse(String serialized) throws MalformedValueException {
    Map<String, Object> result = new HashMap<>();
    String[] values = serialized.split(separator, -1);
    // Key value pairs -> odd length not allowed
    if (values.length % 2 != 0)
      throw new MalformedValueException("Key value pair Object must have odd number of deserialized values");
    for (int i = 0; i < values.length; i += 2) {
      // empty key not allowed!
      if (values[i].length() == 0) {
        throw new MalformedValueException("Empty key not allowed");
      } else {
        result.put(values[i], parseField(values[i], values[i + 1]));
      }
    }
    return new JsonObject(result);
  }

  @Override
  protected boolean isSerializedEmpty(String serialized) {
    return serialized.isEmpty();
  }
}

package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ValueParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class SplitterCharObjectParser implements ValueParser {

  private String separator;
  private Map<String, ValueParser> propertiesParsers;
  private Map<Pattern, ValueParser> patternPropertiesParsers;
  private ValueParser additionalPropertiesParsers;

  public SplitterCharObjectParser(String separator, Map<String, ValueParser> propertiesParsers, Map<Pattern, ValueParser> patternPropertiesParsers, ValueParser additionalPropertiesParsers) {
    this.separator = separator;
    this.propertiesParsers = propertiesParsers;
    this.patternPropertiesParsers = patternPropertiesParsers;
    this.additionalPropertiesParsers = additionalPropertiesParsers;
  }

  @Override
  public Object parse(String serialized) throws MalformedValueException {
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

  private Object parseField(String key, String serialized) {
    if (serialized.isEmpty()) return null;
    if (propertiesParsers != null && propertiesParsers.containsKey(key)) return propertiesParsers.get(key).parse(serialized);
    if (patternPropertiesParsers != null) {
      Optional<ValueParser> p = patternPropertiesParsers
        .entrySet()
        .stream()
        .filter(e -> e.getKey().matcher(key).find())
        .map(Map.Entry::getValue)
        .findFirst();
      if (p.isPresent())
        return p.get().parse(serialized);
    }
    if (additionalPropertiesParsers != null)
      return additionalPropertiesParsers.parse(serialized);
    throw new MalformedValueException("Unrecognized key " + key);
  }
}

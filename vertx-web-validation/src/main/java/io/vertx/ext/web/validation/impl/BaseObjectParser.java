package io.vertx.ext.web.validation.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ValueParser;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class BaseObjectParser implements ValueParser {

  private Map<String, ValueParser> propertiesParsers;
  private Map<Pattern, ValueParser> patternPropertiesParsers;
  private ValueParser additionalPropertiesParsers;

  public BaseObjectParser(Map<String, ValueParser> propertiesParsers, Map<Pattern, ValueParser> patternPropertiesParsers, ValueParser additionalPropertiesParsers) {
    this.propertiesParsers = propertiesParsers;
    this.patternPropertiesParsers = patternPropertiesParsers;
    this.additionalPropertiesParsers = additionalPropertiesParsers;
  }

  @Override
  public abstract @Nullable JsonObject parse(String serialized) throws MalformedValueException;

  Object parseField(String key, String serialized) {
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

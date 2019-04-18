package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ValueParser;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class ObjectParser<X> {

  private Map<String, ValueParser<X>> propertiesParsers;
  private Map<Pattern, ValueParser<X>> patternPropertiesParsers;
  private ValueParser<X> additionalPropertiesParsers;

  public ObjectParser(Map<String, ValueParser<X>> propertiesParsers, Map<Pattern, ValueParser<X>> patternPropertiesParsers, ValueParser<X> additionalPropertiesParsers) {
    this.propertiesParsers = propertiesParsers;
    this.patternPropertiesParsers = patternPropertiesParsers;
    this.additionalPropertiesParsers = additionalPropertiesParsers;
  }

  protected Object parseField(String key, X serialized) {
    if (serialized == null || isSerializedEmpty(serialized)) return null;
    if (propertiesParsers != null && propertiesParsers.containsKey(key)) return propertiesParsers.get(key).parse(serialized);
    if (patternPropertiesParsers != null) {
      Optional<ValueParser<X>> p = patternPropertiesParsers
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
    return null;
  }

  protected abstract boolean isSerializedEmpty(X serialized);

}

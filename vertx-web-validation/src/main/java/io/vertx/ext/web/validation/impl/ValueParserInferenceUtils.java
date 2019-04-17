package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.ValueParser;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.vertx.ext.web.validation.ValueParser.*;

public class ValueParserInferenceUtils {

  protected static ValueParser infeerPrimitiveParser(Object schema) {
    if (schema == null) return null;
    if (!(schema instanceof JsonObject)) return NOOP_PARSER;
    String type = ((JsonObject) schema).getString("type");
    switch (type) {
      case "integer":
        return LONG_PARSER;
      case "number":
        return DOUBLE_PARSER;
      case "boolean":
        return BOOLEAN_PARSER;
      default:
        return NOOP_PARSER;
    }
  }

  public static Map<String, ValueParser> infeerPropertiesParsersForObjectSchema(Object s) {
    JsonObject schema = (JsonObject) s;
    return schema.containsKey("properties") ? schema.getJsonObject("properties")
      .stream()
      .map(e -> new SimpleImmutableEntry<>(e.getKey(), infeerPrimitiveParser(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null;

  }

  public static Map<Pattern, ValueParser> infeerPatternPropertiesParsersForObjectSchema(Object s) {
    JsonObject schema = (JsonObject) s;
    return schema.containsKey("patternProperties") ? schema.getJsonObject("patternProperties")
      .stream()
      .map(e -> new SimpleImmutableEntry<>(Pattern.compile(e.getKey()), infeerPrimitiveParser(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null;
  }

  public static ValueParser infeerAdditionalPropertiesParserForObjectSchema(Object s) {
    try {
      return infeerPrimitiveParser(((JsonObject)s).getJsonObject("additionalProperties"));
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  public static ValueParser infeerItemsParserForArraySchema(Object s) {
    try {
      return infeerPrimitiveParser(((JsonObject)s).getJsonObject("items"));
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  public static List<ValueParser> infeerItemByItemParsersForArraySchema(Object s) {
    try {
      return ((JsonObject) s)
        .getJsonArray("items")
        .stream()
        .map(ValueParserInferenceUtils::infeerPrimitiveParser)
        .collect(Collectors.toList());
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  public static ValueParser infeerAdditionalItemsParserForArraySchema(Object s) {
    try {
      return infeerPrimitiveParser(((JsonObject)s).getJsonObject("additionalItems"));
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

}

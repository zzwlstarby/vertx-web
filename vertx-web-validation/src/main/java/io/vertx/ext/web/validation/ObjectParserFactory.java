package io.vertx.ext.web.validation;

import io.vertx.core.json.JsonObject;

import java.util.Map;

@FunctionalInterface
public interface ObjectParserFactory {

  ParameterParser<JsonObject> newArrayParser(Map<String, ParameterParser> fieldsParser);

  static ObjectParserFactory commaSeparatedObjectParser() {
    return null;
  }

}

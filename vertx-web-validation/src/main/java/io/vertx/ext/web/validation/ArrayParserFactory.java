package io.vertx.ext.web.validation;

import io.vertx.core.json.JsonArray;

@FunctionalInterface
public interface ArrayParserFactory {

  ParameterParser<JsonArray> newArrayParser(ParameterParser itemsParser);

  static ArrayParserFactory commaSeparatedArrayParser() {
    return null;
  }

}

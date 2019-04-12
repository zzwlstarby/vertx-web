package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.web.validation.ValueParser;

@FunctionalInterface
public interface ArrayParserFactory {

  ValueParser newArrayParser(ValueParser itemsParser);

  static ArrayParserFactory commaSeparatedArrayParser() {
    return null;
  }

}

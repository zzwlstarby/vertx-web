package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.impl.SplitterCharArrayParser;

@FunctionalInterface
public interface ArrayParserFactory {

  ValueParser<String> newArrayParser(ValueParser<String> itemsParser);

  static ArrayParserFactory commaSeparatedArrayParser() {
    return itemsParser -> new SplitterCharArrayParser(itemsParser, ",");
  }

}

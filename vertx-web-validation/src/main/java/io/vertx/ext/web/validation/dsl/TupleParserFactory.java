package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.impl.SplitterCharTupleParser;

import java.util.List;

@FunctionalInterface
public interface TupleParserFactory {

  ValueParser<String> newTupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser);

  static TupleParserFactory commaSeparatedTupleParser() {
    return (itemsParser, additionalItemsParser) -> new SplitterCharTupleParser(itemsParser, additionalItemsParser, ",");
  }

}

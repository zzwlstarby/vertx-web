package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.impl.SplitterCharArrayParser;
import io.vertx.ext.web.validation.impl.SplitterCharTupleParser;

import java.util.List;

public interface ArrayParserFactory {

  ValueParser<String> newArrayParser(ValueParser<String> itemsParser);
  ValueParser<String> newTupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser);

  static ArrayParserFactory commaSeparatedArrayParser() {
    return new ArrayParserFactory() {
      @Override
      public ValueParser<String> newArrayParser(ValueParser<String> itemsParser) {
        return new SplitterCharArrayParser(itemsParser, ",");
      }

      @Override
      public ValueParser<String> newTupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser) {
        return new SplitterCharTupleParser(itemsParser, additionalItemsParser, ",");
      }
    };
  }

}

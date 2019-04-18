package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ValueParser;

import java.util.List;
import java.util.stream.Stream;

public abstract class TupleParser<X> {

  private final ValueParser<X>[] itemsParser;
  private final ValueParser<X> additionalItemsParser;

  @SuppressWarnings("unchecked")
  public TupleParser(List<ValueParser<X>> itemsParser, ValueParser<X> additionalItemsParser) {
    this.itemsParser = (ValueParser<X>[]) itemsParser.toArray();
    this.additionalItemsParser = additionalItemsParser;
  }

  protected Stream<Object> parseItem(int i, X serialized) {
    if (i < itemsParser.length)
      return Stream.of(parseValue(serialized, itemsParser[i]));
    else if (additionalItemsParser != null)
      return Stream.of(parseValue(serialized, additionalItemsParser));
    else
      return null;
  }

  private Object parseValue(X v, ValueParser<X> parser) {
    return isSerializedEmpty(v) ? null : parser.parse(v);
  }

  protected abstract boolean isSerializedEmpty(X serialized);
}

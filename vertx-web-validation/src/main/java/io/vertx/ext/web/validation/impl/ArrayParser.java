package io.vertx.ext.web.validation.impl;

import io.vertx.ext.web.validation.ValueParser;

public abstract class ArrayParser<X> {

  private final ValueParser<X> itemsParser;

  public ArrayParser(ValueParser<X> itemsParser) {
    this.itemsParser = itemsParser;
  }

  protected Object parseValue(X v) {
    return isSerializedEmpty(v) ? null : itemsParser.parse(v);
  }

  protected abstract boolean isSerializedEmpty(X serialized);
}

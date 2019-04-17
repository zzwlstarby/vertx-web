package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ValueParser;

import java.util.Arrays;

public class SplitterCharArrayParser implements ValueParser {

  private ValueParser itemsParser;
  private String separator;

  public SplitterCharArrayParser(ValueParser itemsParser, String separator) {
    this.itemsParser = itemsParser;
    this.separator = separator;
  }

  @Override
  public JsonArray parse(String serialized) throws MalformedValueException {
    return Arrays
      .stream(serialized.split(separator, -1))
      .map(this::parseValue)
      .reduce(new JsonArray(), JsonArray::add, JsonArray::addAll);
  }

  private Object parseValue(String v) {
    return v.isEmpty() ? null : itemsParser.parse(v);
  }

}

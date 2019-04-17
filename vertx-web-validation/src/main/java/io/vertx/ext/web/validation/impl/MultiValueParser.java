package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.ValueParser;

import java.util.List;

public class MultiValueParser {

  private boolean expectedArray;
  private ValueParser innerValueParser;

  public MultiValueParser(boolean expectedArray, ValueParser innerValueParser) {
    this.expectedArray = expectedArray;
    this.innerValueParser = innerValueParser;
  }

  public Object parse(List<String> values) {
    if (expectedArray)
      return values.stream().map(innerValueParser::parse).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    else
      return innerValueParser.parse(values.get(0));
  }

}

package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.ValueParser;

import java.util.List;
import java.util.Objects;

public class FormValueParser implements ValueParser<List<String>> {

  private boolean expectedArray;
  private ValueParser<String> innerValueParser;

  public FormValueParser(boolean expectedArray, ValueParser<String> innerValueParser) {
    this.expectedArray = expectedArray;
    this.innerValueParser = innerValueParser;
  }

  public Object parse(List<String> values) {
    if (expectedArray)
      return values.stream().map(innerValueParser::parse).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    else
      return innerValueParser.parse(values.get(0));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FormValueParser that = (FormValueParser) o;
    return expectedArray == that.expectedArray &&
      Objects.equals(innerValueParser, that.innerValueParser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expectedArray, innerValueParser);
  }
}

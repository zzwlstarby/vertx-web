package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ValueParser;

import java.util.List;

public class SplitterCharTupleParser extends TupleParser<String> implements ValueParser<String> {

  private final String separator;

  public SplitterCharTupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser, String separator) {
    super(itemsParser, additionalItemsParser);
    this.separator = separator;
  }

  @Override
  public JsonArray parse(String serialized) throws MalformedValueException {
    JsonArray result = new JsonArray();
    String[] splitted = serialized.split(separator, -1);
    for (int i = 0; i < splitted.length; i++) {
      parseItem(i, splitted[i]).forEach(result::add);
    }
    return result;
  }

  @Override
  protected boolean isSerializedEmpty(String serialized) {
    return serialized.isEmpty();
  }
}

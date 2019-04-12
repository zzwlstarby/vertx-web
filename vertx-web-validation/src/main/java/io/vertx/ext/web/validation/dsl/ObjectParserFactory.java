package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.web.validation.ValueParser;

import java.util.Map;

@FunctionalInterface
public interface ObjectParserFactory {

  ValueParser newArrayParser(Map<String, ValueParser> fieldsParser);

  static ObjectParserFactory commaSeparatedObjectParser() {
    return null;
  }

}

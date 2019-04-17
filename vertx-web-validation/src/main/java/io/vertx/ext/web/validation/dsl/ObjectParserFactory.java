package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.impl.SplitterCharObjectParser;

import java.util.Map;
import java.util.regex.Pattern;

@FunctionalInterface
public interface ObjectParserFactory {

  ValueParser newArrayParser(Map<String, ValueParser> propertiesParser, Map<Pattern, ValueParser> patternPropertiesParser, ValueParser additionalPropertiesParser);

  static ObjectParserFactory commaSeparatedObjectParser() {
    return (propertiesParser, patternPropertiesParser, additionalPropertiesParser) -> new SplitterCharObjectParser(propertiesParser, patternPropertiesParser, additionalPropertiesParser, ",");
  }

}

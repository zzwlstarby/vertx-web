package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;

public interface ValueParser {

  @Nullable Object parse(String serialized) throws MalformedValueException;

  ValueParser NOOP_PARSER = v -> v;
  ValueParser LONG_PARSER = l -> {
    try {
      return Long.parseLong(l);
    } catch (NumberFormatException e) {
      throw new MalformedValueException(e);
    }
  };
  ValueParser DOUBLE_PARSER = d -> {
    try {
      return Double.parseDouble(d);
    } catch (NumberFormatException e) {
      throw new MalformedValueException(e);
    }
  };
  ValueParser BOOLEAN_PARSER = Boolean::parseBoolean;
  ValueParser JSON = j -> {
    try {
      return Json.decodeValue(j);
    } catch (DecodeException e) {
      throw new MalformedValueException(e);
    }
  };

}

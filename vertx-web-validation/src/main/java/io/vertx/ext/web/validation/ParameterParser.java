package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;

import java.util.List;
import java.util.Map;

//TODO document that this thing extract param and transform it to json
@VertxGen
public interface ParameterParser extends Comparable<ParameterParser> {

  @GenIgnore // TODO solve gen issue
  @Nullable Object parseParameter(Map<String, List<String>> parameterValue) throws MalformedValueException;

}

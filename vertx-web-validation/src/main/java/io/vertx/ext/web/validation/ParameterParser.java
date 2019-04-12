package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.Nullable;

import java.util.List;
import java.util.Map;


// extract param and transform it to json
@FunctionalInterface
public interface ParameterParser {

  @Nullable Object parseParameter(Map<String, List<String>> parameterValue) throws MalformedValueException;

}

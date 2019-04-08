package io.vertx.ext.web.validation;

import java.util.List;

@FunctionalInterface
public interface ParameterParser<T> {

  T parseParameter(List<String> parameterValue);

}

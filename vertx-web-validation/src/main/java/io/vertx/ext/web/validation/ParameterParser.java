package io.vertx.ext.web.validation;

@FunctionalInterface
public interface ParameterParser<T> {

  T parseParameter(String parameterValue);

}

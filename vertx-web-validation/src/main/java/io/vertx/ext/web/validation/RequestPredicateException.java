package io.vertx.ext.web.validation;

public class RequestPredicateException extends BadRequestException {
  public RequestPredicateException(String message) {
    super(message, null);
  }

  public RequestPredicateException(String message, Throwable cause) {
    super(message, cause);
  }
}

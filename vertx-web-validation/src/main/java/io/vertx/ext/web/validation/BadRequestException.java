package io.vertx.ext.web.validation;

import io.vertx.core.VertxException;

public abstract class BadRequestException extends VertxException {

  public BadRequestException(String message, Throwable cause) {
    super("Bad Request: " + message, cause);
  }

}

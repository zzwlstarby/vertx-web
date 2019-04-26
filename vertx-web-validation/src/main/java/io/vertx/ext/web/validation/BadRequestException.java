package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.VertxException;

@DataObject
public abstract class BadRequestException extends VertxException {

  public BadRequestException(String message, Throwable cause) {
    super("Bad Request: " + message, cause);
  }

}

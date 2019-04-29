package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;

@DataObject
public abstract class BadRequestException extends VertxException {

  public BadRequestException(String message, Throwable cause) {
    super("[Bad Request] " + message, cause);
  }

  public JsonObject toJson() {
    JsonObject res = new JsonObject()
      .put("type", this.getClass().getSimpleName())
      .put("message", this.getMessage());
    if (this.getCause() != null) {
      res
        .put("causeType", this.getCause().getClass().getSimpleName())
        .put("causeMessage", this.getCause().getMessage());
    }
    return res;
  }

}

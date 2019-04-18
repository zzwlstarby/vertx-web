package io.vertx.ext.web.validation;

import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;
import java.util.regex.Pattern;

public interface RequestPredicate {

  boolean test(RoutingContext context);

  static RequestPredicate BODY_REQUIRED = rc -> rc.getBody() != null;

  static RequestPredicate fileUploadExists(String propertyName, Pattern contentType) {
    return rc -> {
      Set<FileUpload> files = rc.fileUploads();
      for (FileUpload f : files) {
        if (f.name().equals(propertyName) && contentType.matcher(f.contentType()).matches()) return true;
      }
      return false;
    };
  }

}

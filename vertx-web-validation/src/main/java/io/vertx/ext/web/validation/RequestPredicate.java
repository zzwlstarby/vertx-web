package io.vertx.ext.web.validation;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.vertx.ext.web.validation.RequestPredicateResult.failed;
import static io.vertx.ext.web.validation.RequestPredicateResult.success;

public interface RequestPredicate extends Function<RoutingContext, RequestPredicateResult> {

  RequestPredicate BODY_REQUIRED = rc -> {
    if (!rc.request().headers().contains(HttpHeaders.CONTENT_TYPE)) return failed("Body required");
    return success();
  };

  static RequestPredicate multipartFileUploadExists(String propertyName, Pattern contentType) {
    return rc -> {
      if (
        rc.request().headers().contains(HttpHeaders.CONTENT_TYPE) &&
        rc.request().getHeader(HttpHeaders.CONTENT_TYPE).contains("multipart/form-data")
      ) {
        Set<FileUpload> files = rc.fileUploads();
        for (FileUpload f : files) {
          if (f.name().equals(propertyName) && contentType.matcher(f.contentType()).matches()) return success();
        }
        return failed(String.format("File with content type %s and name %s is missing", contentType.toString(), propertyName));
      } else return success();
    };
  }

}

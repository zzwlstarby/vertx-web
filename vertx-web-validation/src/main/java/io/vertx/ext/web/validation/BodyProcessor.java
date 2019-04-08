package io.vertx.ext.web.validation;

import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.SchemaBuilder;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Future;

public interface BodyProcessor<T> {

  boolean canProcess(RoutingContext requestContext);

  Future<RequestParameter> process(RoutingContext requestContext);

  static <T> BodyProcessor<T> json(Validator<T> validator) {
    return null;
  }

  static <T> BodyProcessor<T> json(SchemaBuilder schemaBuilder) {
    return null;
  }

  static <T> BodyProcessor<T> formUrlEncoded(ObjectSchemaBuilder schemaBuilder) { return null; }

  static <T> BodyProcessor<T> multipartFormData(ObjectSchemaBuilder schemaBuilder) { return null; } // TODO file uploads?

}

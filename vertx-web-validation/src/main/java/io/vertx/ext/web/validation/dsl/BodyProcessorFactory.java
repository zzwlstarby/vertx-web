package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.StringSchemaBuilder;
import io.vertx.ext.web.validation.BodyProcessor;

public interface BodyProcessorFactory {

  BodyProcessor create(SchemaParser parser);

  static BodyProcessor json(ObjectSchemaBuilder schemaBuilder) {
    return null;
  }

  static BodyProcessor json(ArraySchemaBuilder schemaBuilder) {
    return null;
  }

  static BodyProcessor textPlain(StringSchemaBuilder schemaBuilder) { return null; }

  static BodyProcessor formUrlEncoded(ObjectSchemaBuilder schemaBuilder) { return null; }

  static BodyProcessor multipartFormData(ObjectSchemaBuilder schemaBuilder) { return null; } // TODO file uploads?


}

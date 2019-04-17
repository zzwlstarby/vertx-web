package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ArraySchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.StringSchemaBuilder;
import io.vertx.ext.web.validation.BodyProcessor;
import io.vertx.ext.web.validation.impl.JsonBodyProcessorImpl;
import io.vertx.ext.web.validation.impl.SchemaValidator;
import io.vertx.ext.web.validation.impl.TextPlainBodyProcessorImpl;

public interface BodyProcessorFactory {

  BodyProcessor create(SchemaParser parser);

  static BodyProcessorFactory json(ObjectSchemaBuilder schemaBuilder) {
    return parser -> new JsonBodyProcessorImpl(new SchemaValidator(schemaBuilder.build(parser)));
  }

  static BodyProcessorFactory json(ArraySchemaBuilder schemaBuilder) {
    return parser -> new JsonBodyProcessorImpl(new SchemaValidator(schemaBuilder.build(parser)));
  }

  static BodyProcessorFactory textPlain(StringSchemaBuilder schemaBuilder) {
    return parser -> new TextPlainBodyProcessorImpl(new SchemaValidator(schemaBuilder.build(parser)));
  }

  static BodyProcessorFactory formUrlEncoded(ObjectSchemaBuilder schemaBuilder) {

  }

  static BodyProcessorFactory multipartFormData(ObjectSchemaBuilder schemaBuilder) { return null; }

  static BodyProcessorFactory multipartFileUpload(String fileName, String expectedContentType) { return null; }

}

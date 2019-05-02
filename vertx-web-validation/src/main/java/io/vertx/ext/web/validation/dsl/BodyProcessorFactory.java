package io.vertx.ext.web.validation.dsl;

import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.generic.dsl.ObjectSchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.SchemaBuilder;
import io.vertx.ext.json.schema.generic.dsl.StringSchemaBuilder;
import io.vertx.ext.web.validation.BodyProcessor;
import io.vertx.ext.web.validation.impl.*;

public interface BodyProcessorFactory {

  BodyProcessor create(SchemaParser parser);

  static BodyProcessorFactory json(SchemaBuilder schemaBuilder) {
    return parser -> new JsonBodyProcessorImpl(new SchemaValidator(schemaBuilder.build(parser)));
  }

  static BodyProcessorFactory textPlain(StringSchemaBuilder schemaBuilder) {
    return parser -> new TextPlainBodyProcessorImpl(new SchemaValidator(schemaBuilder.build(parser)));
  }

  static BodyProcessorFactory formUrlEncoded(ObjectSchemaBuilder schemaBuilder) {
    return parser -> {
      Schema s = schemaBuilder.build(parser);
      Object jsonSchema = s.getJson();
      return new FormBodyProcessorImpl(
        ValueParserInferenceUtils.infeerPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerPatternPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerAdditionalPropertiesFormValueParserForObjectSchema(jsonSchema),
        "application/x-www-form-urlencoded",
        new SchemaValidator(s)
      );
    };
  }

  static BodyProcessorFactory multipartFormData(ObjectSchemaBuilder schemaBuilder) {
    return parser -> {
      Schema s = schemaBuilder.build(parser);
      Object jsonSchema = s.getJson();
      return new FormBodyProcessorImpl(
        ValueParserInferenceUtils.infeerPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerPatternPropertiesFormValueParserForObjectSchema(jsonSchema),
        ValueParserInferenceUtils.infeerAdditionalPropertiesFormValueParserForObjectSchema(jsonSchema),
        "multipart/form-data",
        new SchemaValidator(s)
      );
    };
  }

}

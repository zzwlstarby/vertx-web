package io.vertx.ext.web.validation.dsl;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.validation.ParameterLocation;
import io.vertx.ext.web.validation.ParameterProcessor;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.dsl.impl.ValidationHandlerBuilderImpl;

public interface ValidationHandlerBuilder {

  @Fluent
  ValidationHandlerBuilder parameter(ParameterLocation location, ParameterProcessor processor);

  @Fluent
  ValidationHandlerBuilder queryParameter(StyledParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder queryParameter(SimpleParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder pathParameter(SimpleParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder cookieParameter(StyledParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder cookieParameter(SimpleParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder headerParameter(SimpleParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder body(BodyProcessorFactory bodyProcessor);

  @Fluent
  ValidationHandlerBuilder bodyRequired(boolean bodyRequired);

  ValidationHandler build();

  static ValidationHandlerBuilder create(SchemaParser parser) {
    return new ValidationHandlerBuilderImpl(parser);
  }

}

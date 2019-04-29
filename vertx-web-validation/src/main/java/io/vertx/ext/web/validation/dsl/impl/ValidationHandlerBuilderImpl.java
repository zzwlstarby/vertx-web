package io.vertx.ext.web.validation.dsl.impl;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.*;
import io.vertx.ext.web.validation.dsl.BodyProcessorFactory;
import io.vertx.ext.web.validation.dsl.SimpleParameterProcessorFactory;
import io.vertx.ext.web.validation.dsl.StyledParameterProcessorFactory;
import io.vertx.ext.web.validation.dsl.ValidationHandlerBuilder;
import io.vertx.ext.web.validation.impl.ValidationHandlerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ValidationHandlerBuilderImpl implements ValidationHandlerBuilder {

  SchemaParser jsonSchemaParser;

  Map<ParameterLocation, List<ParameterProcessor>> parameterProcessors = new HashMap<>();
  List<BodyProcessor> bodyProcessors = new ArrayList<>();
  List<Function<RoutingContext, RequestPredicateResult>> predicates = new ArrayList<>();

  public ValidationHandlerBuilderImpl(SchemaParser jsonSchemaParser) {
    this.jsonSchemaParser = jsonSchemaParser;
  }

  @Override
  public ValidationHandlerBuilder parameter(ParameterLocation location, ParameterProcessor processor) {
    parameterProcessors.computeIfAbsent(location, k -> new ArrayList<>()).add(processor);
    return this;
  }

  @Override
  public ValidationHandlerBuilder queryParameter(StyledParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.QUERY, parameterProcessor.create(ParameterLocation.QUERY, jsonSchemaParser));
  }

  @Override
  public ValidationHandlerBuilder queryParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.QUERY, parameterProcessor.create(ParameterLocation.QUERY, jsonSchemaParser));
  }

  @Override
  public ValidationHandlerBuilder pathParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.PATH, parameterProcessor.create(ParameterLocation.PATH, jsonSchemaParser));
  }

  @Override
  public ValidationHandlerBuilder cookieParameter(StyledParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.COOKIE, parameterProcessor.create(ParameterLocation.COOKIE, jsonSchemaParser));
  }

  @Override
  public ValidationHandlerBuilder cookieParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.COOKIE, parameterProcessor.create(ParameterLocation.COOKIE, jsonSchemaParser));
  }

  @Override
  public ValidationHandlerBuilder headerParameter(SimpleParameterProcessorFactory parameterProcessor) {
    return parameter(ParameterLocation.HEADER, parameterProcessor.create(ParameterLocation.HEADER, jsonSchemaParser));
  }

  @Override
  public ValidationHandlerBuilder body(BodyProcessorFactory bodyProcessor) {
    bodyProcessors.add(bodyProcessor.create(jsonSchemaParser));
    return this;
  }

  @Override
  public ValidationHandlerBuilder predicate(RequestPredicate predicate) {
    predicates.add(predicate);
    return this;
  }

  @Override
  public ValidationHandler build() {
    return new ValidationHandlerImpl(
      parameterProcessors,
      bodyProcessors,
      predicates
    );
  }
}

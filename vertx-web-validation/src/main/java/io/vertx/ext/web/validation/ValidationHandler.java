package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * TODO
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen(concrete = false)
public interface ValidationHandler extends Handler<RoutingContext> {

  //TODO normal query, path, cookie, header parameters has:
  // - parser
  // - schema
  // - isOptional
  // - name

  //TODO body validation differs between content type

  //TODO application/x-www-form-urlencoded is parsed as single json and validated as json works like other params

  //TODO application/json and similar are parsed and validated

  //TODO multipart/form-data and similar are split into file uploads and form attributes

  //TODO params and body states:
  //  1. absent when required
  //  2. parsing failed
  //  3. validation failed
  //  4. ok

  //TODO ValidationHandler must contain methods to add parameter validators and body validators
  //TODO ParameterProcessor must contain boolean canProcess(Map<String, String> params) and Future<RequestParameter> process(Map<String, String> params)
  //TODO BodyProcessor must contain boolean canProcess(RequestContext requestContext) and Future<RequestParameter> process(RequestContext requestContext)

  //TODO features we want to provide:
  // - query, path, cookie, header params
  // - body validation

  //TODO styles?!?!?!
  // - path, header -> simple
  // - query, cookie -> form (simple), exploded form, custom collection parser, deep object

  @Fluent
  ValidationHandler parameter(ParameterLocation location, ParameterProcessor processor);

  @Fluent
  default ValidationHandler queryParameter(StyledParameterProcessor parameterProcessor) {
    return parameter(ParameterLocation.QUERY, parameterProcessor);
  }

  @Fluent
  default ValidationHandler queryParameter(SimpleParameterProcessor parameterProcessor) {
    return parameter(ParameterLocation.QUERY, parameterProcessor);
  }

  @Fluent
  default ValidationHandler pathParameter(SimpleParameterProcessor parameterProcessor) {
    return parameter(ParameterLocation.PATH, parameterProcessor);
  }

  @Fluent
  default ValidationHandler cookieParameter(StyledParameterProcessor parameterProcessor) {
    return parameter(ParameterLocation.COOKIE, parameterProcessor);
  }

  @Fluent
  default ValidationHandler cookieParameter(SimpleParameterProcessor parameterProcessor) {
    return parameter(ParameterLocation.COOKIE, parameterProcessor);
  }

  @Fluent
  default ValidationHandler headerParameter(SimpleParameterProcessor parameterProcessor) {
    return parameter(ParameterLocation.HEADER, parameterProcessor);
  }

  @Fluent
  ValidationHandler body(BodyProcessor bodyProcessor);

  @Fluent
  ValidationHandler bodyRequired(boolean bodyRequired);

  static ValidationHandler create() {
    return null;
  }

}

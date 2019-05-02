package io.vertx.ext.web.validation;

import io.vertx.core.Handler;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.dsl.ValidationHandlerBuilder;

/**
 * TODO
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
//@VertxGen(concrete = false)
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

  static ValidationHandlerBuilder builder(SchemaParser parser) {
    return ValidationHandlerBuilder.create(parser);
  }

}

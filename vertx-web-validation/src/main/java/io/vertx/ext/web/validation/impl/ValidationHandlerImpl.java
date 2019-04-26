package io.vertx.ext.web.validation.impl;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.validation.*;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public abstract class ValidationHandlerImpl implements ValidationHandler {

  private ParameterProcessor[] queryParameters;
  private ParameterProcessor[] pathParameters;
  private ParameterProcessor[] cookieParameters;
  private ParameterProcessor[] headerParameters;
  private BodyProcessor[] bodyProcessors;
  private Function<RoutingContext, RequestPredicateResult>[] predicates;

  @SuppressWarnings("unchecked")
  public ValidationHandlerImpl(Map<ParameterLocation, List<ParameterProcessor>> parameterProcessors, List<BodyProcessor> bodyProcessors, List<Function<RoutingContext, RequestPredicateResult>> predicates) {
    this.queryParameters = listToArray(parameterProcessors.get(ParameterLocation.QUERY));
    this.pathParameters = listToArray(parameterProcessors.get(ParameterLocation.PATH));
    this.cookieParameters = listToArray(parameterProcessors.get(ParameterLocation.COOKIE));
    this.headerParameters = listToArray(parameterProcessors.get(ParameterLocation.HEADER));
    this.bodyProcessors = listToArray(bodyProcessors);
    this.predicates = listToArray(predicates);
  }

  @SuppressWarnings("unchecked")
  private <T> T[] listToArray(List<T> l) {
    if (l == null || l.isEmpty()) return null;
    else return (T[]) l.toArray();
  }

  @Override
  public void handle(RoutingContext routingContext) {
    try {
      runPredicates(routingContext);

      RequestParametersImpl parsedParameters = new RequestParametersImpl();

      parsedParameters.setPathParameters(validatePathParams(routingContext));
      parsedParameters.setQueryParameters(validateQueryParams(routingContext));
      parsedParameters.setHeaderParameters(validateHeaderParams(routingContext));
      parsedParameters.setCookieParameters(validateCookieParams(routingContext));

    } catch (BadRequestException e) {
      routingContext.fail(400, e);
    }
    try {
      RequestParametersImpl parsedParameters = new RequestParametersImpl();

      parsedParameters.setPathParameters(validatePathParams(routingContext));
      parsedParameters.setQueryParameters(validateQueryParams(routingContext));
      parsedParameters.setHeaderParameters(validateHeaderParams(routingContext));
      parsedParameters.setCookieParameters(validateCookieParams(routingContext));

      //Run custom validators
      for (CustomValidator customValidator : customValidators) {
        customValidator.validate(routingContext);
      }

      String contentType = routingContext.request().getHeader("Content-Type");
      if (contentType != null && contentType.length() != 0) {
        boolean isMultipart = contentType.contains("multipart/form-data");

        if (multipartFileRules.size() != 0 && !isMultipart) {
          throw ValidationException.ValidationExceptionFactory.generateWrongContentTypeExpected(contentType,
            "multipart/form-data");
        }
        if (contentType.contains("application/x-www-form-urlencoded")) {
          parsedParameters.setFormParameters(validateFormParams(routingContext));
        } else if (isMultipart) {
          parsedParameters.setFormParameters(validateFormParams(routingContext));
          validateFileUpload(routingContext);
        } else if (Utils.isJsonContentType(contentType) || Utils.isXMLContentType(contentType)) {
          parsedParameters.setBody(validateEntireBody(routingContext));
        } else if (bodyRequired && !checkContentType(contentType)) {
          throw ValidationException.ValidationExceptionFactory.generateWrongContentTypeExpected(contentType, null);
        } // If content type is valid or body is not required, do nothing!
      } else if (bodyRequired) {
        throw ValidationException.ValidationExceptionFactory.generateWrongContentTypeExpected(contentType, null);
      }

      if (routingContext.data().containsKey("parsedParameters")) {
        ((RequestParametersImpl)routingContext.get("parsedParameters")).merge(parsedParameters);
      } else {
        routingContext.put("parsedParameters", parsedParameters);
      }
      routingContext.next();

    } catch (BadRequestException e) {
      routingContext.fail(400, e);
    }
  }

  private void runPredicates(RoutingContext context) throws BadRequestException {
    for (Function<RoutingContext, RequestPredicateResult> p : predicates) {
      RequestPredicateResult res = p.apply(context);
      if (!res.succeded()) throw new RequestPredicateException(res.getErrorMessage());
    }
  }


  private Future<Map<String, RequestParameter>> validatePathParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    if (pathParameters == null) return Future.succeededFuture(new HashMap<>());

    Map<String, List<String>> pathParams = routingContext
      .pathParams()
      .entrySet()
      .stream()
      .map(e -> new SimpleImmutableEntry<>(e.getKey(), Collections.singletonList(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<String, RequestParameter> parsedParams = new HashMap<>();

    return processParams(parsedParams, pathParams, pathParameters);
  }

  private Future<Map<String, RequestParameter>> validateCookieParams(RoutingContext routingContext) {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    if (cookieParameters == null) return Future.succeededFuture(new HashMap<>());

    if (!routingContext.request().headers().contains("Cookie"))
      return Future.succeededFuture(new HashMap<>());
    QueryStringDecoder decoder = new QueryStringDecoder("/?" + routingContext.request().getHeader("Cookie")); // Some hack to reuse this object
    Map<String, List<String>> cookies = new HashMap<>();
    for (Map.Entry<String, List<String>> e : decoder.parameters().entrySet()) {
      String key = e.getKey().trim();
      if (cookies.containsKey(key))
        cookies.get(key).addAll(e.getValue());
      else
        cookies.put(key, e.getValue());
    }
    Map<String, RequestParameter> parsedParams = new HashMap<>();

    return processParams(parsedParams, cookies, cookieParameters);
  }

  private Map<String, RequestParameter> validateQueryParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    MultiMap queryParams = new CaseInsensitiveHeaders().addAll(routingContext.queryParams());
    for (ParameterValidationRule rule : queryParamsRules.values()) {
      String name = rule.getName();
      if (queryParams.contains(name)) {
        List<String> p = queryParams.getAll(name);
        queryParams.remove(name);
        if (p.size() != 0) {
          RequestParameter parsedParam = rule.validateArrayParam(p);
          if (parsedParams.containsKey(parsedParam.getName()))
            parsedParam = parsedParam.merge(parsedParams.get(parsedParam.getName()));
          parsedParams.put(parsedParam.getName(), parsedParam);
        } else {
          throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(name + " can't be empty");
        }
      } else if (rule.parameterTypeValidator().getDefault() != null) {
        RequestParameter parsedParam = new RequestParameterImpl(name, rule.parameterTypeValidator().getDefault());
        if (parsedParams.containsKey(parsedParam.getName()))
          parsedParam = parsedParam.merge(parsedParams.get(parsedParam.getName()));
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (!rule.isOptional())
        throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name,
          ParameterLocation.QUERY);
    }
    if (queryAdditionalPropertiesValidator != null) {
      for (Map.Entry<String, String> e : queryParams.entries()) {
        try {
          Map<String, RequestParameter> r = new HashMap<>();
          r.put(e.getKey(), queryAdditionalPropertiesValidator.isValid(e.getValue()));
          RequestParameter parsedParam = new RequestParameterImpl(queryAdditionalPropertiesObjectPropertyName, r);
          if (parsedParams.containsKey(queryAdditionalPropertiesObjectPropertyName))
            parsedParam = parsedParam.merge(parsedParams.get(queryAdditionalPropertiesObjectPropertyName));
          parsedParams.put(parsedParam.getName(), parsedParam);
        } catch (ValidationException ex) {
          ex.setParameterName(queryAdditionalPropertiesObjectPropertyName);
          e.setValue(e.getValue());
          throw ex;
        }
      }
    }
    return parsedParams;
  }

  private Map<String, RequestParameter> validateHeaderParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    MultiMap headersParams = routingContext.request().headers();
    for (ParameterValidationRule rule : headerParamsRules.values()) {
      String name = rule.getName();
      if (headersParams.contains(name)) {
        List<String> p = headersParams.getAll(name);
        if (p.size() != 0) {
          RequestParameter parsedParam = rule.validateArrayParam(p);
          if (parsedParams.containsKey(parsedParam.getName()))
            parsedParam = parsedParam.merge(parsedParams.get(parsedParam.getName()));
          parsedParams.put(parsedParam.getName(), parsedParam);
        } else {
          throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(name + " can't be empty");
        }
      } else if (rule.parameterTypeValidator().getDefault() != null) {
        RequestParameter parsedParam = new RequestParameterImpl(name, rule.parameterTypeValidator().getDefault());
        if (parsedParams.containsKey(parsedParam.getName()))
          parsedParam = parsedParam.merge(parsedParams.get(parsedParam.getName()));
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (!rule.isOptional())
        throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name,
          ParameterLocation.HEADER);
    }
    return parsedParams;
  }

  private Map<String, RequestParameter> validateFormParams(RoutingContext routingContext) throws ValidationException {
    // Validation process validate only params that are registered in the validation -> extra params are allowed
    Map<String, RequestParameter> parsedParams = new HashMap<>();
    MultiMap formParams = routingContext.request().formAttributes();
    for (ParameterValidationRule rule : formParamsRules.values()) {
      String name = rule.getName();
      if (formParams.contains(name)) {
        List<String> p = formParams.getAll(name);
        if (p.size() != 0) {
          RequestParameter parsedParam = rule.validateArrayParam(p);
          if (parsedParams.containsKey(parsedParam.getName()))
            parsedParam = parsedParam.merge(parsedParams.get(parsedParam.getName()));
          parsedParams.put(parsedParam.getName(), parsedParam);
        } else {
          throw ValidationException.ValidationExceptionFactory.generateNotMatchValidationException(name + " can't be empty");
        }
      } else if (rule.parameterTypeValidator().getDefault() != null) {
        RequestParameter parsedParam = new RequestParameterImpl(name, rule.parameterTypeValidator().getDefault());
        if (parsedParams.containsKey(parsedParam.getName()))
          parsedParam = parsedParam.merge(parsedParams.get(parsedParam.getName()));
        parsedParams.put(parsedParam.getName(), parsedParam);
      } else if (!rule.isOptional())
        throw ValidationException.ValidationExceptionFactory.generateNotFoundValidationException(name,
          ParameterLocation.BODY_FORM);
    }
    return parsedParams;
  }

  private boolean existFileUpload(Set<FileUpload> files, String name, Pattern contentType) {
    for (FileUpload f : files) {
      if (f.name().equals(name) && contentType.matcher(f.contentType()).matches()) return true;
    }
    return false;
  }

  private void validateFileUpload(RoutingContext routingContext) throws ValidationException {
    Set<FileUpload> fileUploads = routingContext.fileUploads();
    for (Map.Entry<String, Pattern> expectedFile : multipartFileRules.entrySet()) {
      if (!existFileUpload(fileUploads, expectedFile.getKey(), expectedFile.getValue()))
        throw ValidationException.ValidationExceptionFactory.generateFileNotFoundValidationException(expectedFile
          .getKey(), expectedFile.getValue().toString());
    }
  }

  private RequestParameter validateEntireBody(RoutingContext routingContext) throws ValidationException {
    if (entireBodyValidator != null) return entireBodyValidator.isValid(routingContext.getBodyAsString());
    else return RequestParameter.create(null);
  }

  private boolean checkContentType(String contentType) {
    for (String ct : bodyFileRules) {
      if (contentType.contains(ct)) return true;
    }
    return false;
  }

  private Future<Map<String, RequestParameter>> processParams(Map<String, RequestParameter> parsedParams, Map<String, List<String>> params, ParameterProcessor[] processors) {
    Future<Map<String, RequestParameter>> waitingFutureChain = Future.succeededFuture(parsedParams);

    for (ParameterProcessor processor : pathParameters) {
      Future<RequestParameter> fut = processor.process(params);
      if (fut.isComplete()) {
        if (fut.succeeded()) {
          parsedParams.put(processor.getName(), fut.result());
        } else if (fut.failed()) {
          return Future.failedFuture(fut.cause());
        }
      } else {
        if (waitingFutureChain == null) {
          waitingFutureChain = fut.map(rp -> {
            parsedParams.put(processor.getName(), rp);
            return parsedParams;
          });
        } else {
          waitingFutureChain.compose(m -> fut.map(rp -> {
            parsedParams.put(processor.getName(), rp);
            return parsedParams;
          }));
        }
      }
    }

    return Future.succeededFuture(parsedParams);
  }

}

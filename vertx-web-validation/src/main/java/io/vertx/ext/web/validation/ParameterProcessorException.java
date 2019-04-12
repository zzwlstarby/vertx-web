package io.vertx.ext.web.validation;

public class ParameterProcessorException extends BadRequestException {

  public enum ParameterProcessorErrorType {
    MISSING_PARAMETER_WHEN_REQUIRED_ERROR,
    PARSING_ERROR,
    VALIDATION_ERROR
  }

  String parameterName;
  ParameterLocation location;
  ParameterProcessorErrorType errorType;

  public ParameterProcessorException(String message, String parameterName, ParameterLocation location, ParameterProcessorErrorType errorType, Throwable cause) {
    super(message, cause);
    this.parameterName = parameterName;
    this.location = location;
    this.errorType = errorType;
  }

  public String getParameterName() {
    return parameterName;
  }

  public ParameterLocation getLocation() {
    return location;
  }

  public ParameterProcessorErrorType getErrorType() {
    return errorType;
  }

  public static ParameterProcessorException createMissingParameterWhenRequired(String parameterName, ParameterLocation location) {
    return new ParameterProcessorException("Missing parameter " + parameterName + " in " +  location, parameterName, location, ParameterProcessorErrorType.MISSING_PARAMETER_WHEN_REQUIRED_ERROR, null);
  }

  public static ParameterProcessorException createParsingError(String parameterName, ParameterLocation location, MalformedValueException cause) {
    return new ParameterProcessorException(
      String.format("Parsing error for parameter %s in location %s: %s", parameterName, location, cause.getMessage()),
      parameterName, location, ParameterProcessorErrorType.PARSING_ERROR, cause
    );
  }

  public static ParameterProcessorException createValidationError(String parameterName, ParameterLocation location, Throwable cause) {
    return new ParameterProcessorException(
      String.format("Validation error for parameter %s in location %s: %s", parameterName, location, cause.getMessage()),
      parameterName, location, ParameterProcessorErrorType.VALIDATION_ERROR, cause
    );
  }
}

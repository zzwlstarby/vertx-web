package io.vertx.ext.web.validation;

public class BodyProcessorException extends BadRequestException {

  public enum BodyProcessorErrorType {
    PARSING_ERROR,
    VALIDATION_ERROR
  }

  private String contentType;
  private BodyProcessorErrorType errorType;

  public BodyProcessorException(String message, Throwable cause, String contentType, BodyProcessorErrorType errorType) {
    super(message, cause);
    this.contentType = contentType;
    this.errorType = errorType;
  }

  public String getContentType() {
    return contentType;
  }

  public BodyProcessorErrorType getErrorType() {
    return errorType;
  }

  public static BodyProcessorException createParsingError(String contentType, MalformedValueException cause) {
    return new BodyProcessorException(
      String.format("Body %s parsing error: %s", contentType, cause.getMessage()), cause, contentType, BodyProcessorErrorType.PARSING_ERROR
    );
  }

  public static BodyProcessorException createValidationError(String contentType, Throwable cause) {
    return new BodyProcessorException(
      String.format("Validation error for body %s: %s", contentType, cause.getMessage()), cause, contentType, BodyProcessorErrorType.VALIDATION_ERROR
    );
  }
}

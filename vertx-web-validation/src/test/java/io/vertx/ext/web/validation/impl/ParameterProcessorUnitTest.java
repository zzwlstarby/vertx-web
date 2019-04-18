package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.json.schema.*;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.validation.Validator;
import io.vertx.ext.web.validation.*;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ParameterProcessorUnitTest {

  SchemaRouter router;
  SchemaParser parser;

  @Mock ParameterParser mockedParser;
  @Mock Validator mockedValidator;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(new SchemaParserOptions(), router);
  }

  @Test
  public void testRequiredParam(VertxTestContext testContext) {
    ParameterProcessor processor = new ParameterProcessorImpl(
      "myParam",
      ParameterLocation.QUERY,
      false,
      mockedParser,
      mockedValidator
    );

    when(mockedParser.parseParameter(any())).thenReturn(null);

    processor.process(new HashMap<>()).setHandler(testContext.failing(throwable -> {
      testContext.verify(() -> {
        assertThat(throwable)
          .isInstanceOf(ParameterProcessorException.class)
          .hasFieldOrPropertyWithValue("errorType", ParameterProcessorException.ParameterProcessorErrorType.MISSING_PARAMETER_WHEN_REQUIRED_ERROR)
          .hasFieldOrPropertyWithValue("location", ParameterLocation.QUERY)
          .hasFieldOrPropertyWithValue("parameterName", "myParam")
          .hasNoCause();
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testOptionalParam(VertxTestContext testContext) {
    ParameterProcessor processor = new ParameterProcessorImpl(
      "myParam",
      ParameterLocation.QUERY,
      true,
      mockedParser,
      mockedValidator
    );

    when(mockedParser.parseParameter(any())).thenReturn(null);

    processor.process(new HashMap<>()).setHandler(testContext.succeeding(value -> {
      testContext.verify(() ->
        assertThat(value).isNull()
      );
      testContext.completeNow();
    }));
  }

  @Test
  public void testParsingFailure(VertxTestContext testContext) {
    ParameterProcessor processor = new ParameterProcessorImpl(
      "myParam",
      ParameterLocation.QUERY,
      false,
      mockedParser,
      mockedValidator
    );

    when(mockedParser.parseParameter(any())).thenThrow(new MalformedValueException("bla"));

    processor.process(new HashMap<>()).setHandler(testContext.failing(throwable -> {
      testContext.verify(() -> {
        assertThat(throwable)
          .isInstanceOf(ParameterProcessorException.class)
          .hasFieldOrPropertyWithValue("errorType", ParameterProcessorException.ParameterProcessorErrorType.PARSING_ERROR)
          .hasFieldOrPropertyWithValue("location", ParameterLocation.QUERY)
          .hasFieldOrPropertyWithValue("parameterName", "myParam")
          .hasCauseInstanceOf(MalformedValueException.class);
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testValidation(VertxTestContext testContext) {
    ParameterProcessor processor = new ParameterProcessorImpl(
      "myParam",
      ParameterLocation.QUERY,
      true,
      mockedParser,
      mockedValidator
    );

    when(mockedParser.parseParameter(any())).thenReturn("aaa");
    when(mockedValidator.validate(any())).thenReturn(Future.succeededFuture(RequestParameter.create("aaa")));

    processor.process(new HashMap<>()).setHandler(testContext.succeeding(rp -> {
      testContext.verify(() -> {
        assertThat(rp.isString()).isTrue();
        assertThat(rp.getString()).isEqualTo("aaa");
      });
      testContext.completeNow();
    }));
  }

  @Test
  public void testValidationFailure(VertxTestContext testContext) {
    ParameterProcessor processor = new ParameterProcessorImpl(
      "myParam",
      ParameterLocation.QUERY,
      true,
      mockedParser,
      mockedValidator
    );

    when(mockedParser.parseParameter(any())).thenReturn("aaa");
    when(mockedValidator.validate(any())).thenReturn(Future.failedFuture(ValidationException.createException("aaa", "aaa", "aaa")));

    processor.process(new HashMap<>()).setHandler(testContext.failing(throwable -> {
      testContext.verify(() -> {
        assertThat(throwable)
          .isInstanceOf(ParameterProcessorException.class)
          .hasFieldOrPropertyWithValue("errorType", ParameterProcessorException.ParameterProcessorErrorType.VALIDATION_ERROR)
          .hasFieldOrPropertyWithValue("location", ParameterLocation.QUERY)
          .hasFieldOrPropertyWithValue("parameterName", "myParam")
          .hasCauseInstanceOf(ValidationException.class);
      });
      testContext.completeNow();
    }));
  }
}

package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaParserOptions;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.ValueParser;
import io.vertx.ext.web.validation.testutils.TestParsers;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(VertxExtension.class)
public class ExplodedObjectValueParameterParserTest {

  SchemaRouter router;
  SchemaParser parser;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(new SchemaParserOptions(), router);
  }

  @Test
  public void testValid() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList("2.1"));
    map.put("prop3", singletonList("aaa"));
    map.put("prop4", singletonList("true"));
    map.put("other", singletonList("hello"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello"))
      );

    assertThat(map)
      .isEmpty();
  }

  @Test
  public void testNoAdditionalProperties() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      null,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList("2.1"));
    map.put("prop3", singletonList("aaa"));
    map.put("prop4", singletonList("true"));
    map.put("other", singletonList("hello"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT)
      );

    assertThat(map)
      .containsKey("other");
  }

  @Test
  public void testInvalid() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList("2.1"));
    map.put("prop3", singletonList("aaa"));
    map.put("prop4", singletonList("true"));
    map.put("other", singletonList("hello"));

    assertThatExceptionOfType(MalformedValueException.class)
      .isThrownBy(() -> parser.parseParameter(map));
  }

}

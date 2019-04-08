package io.vertx.ext.web.validation.impl;

import io.vertx.core.Future;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.SimpleParameterProcessor;
import io.vertx.ext.web.validation.StyledParameterProcessor;

import java.util.Map;

public class StyledParameterProcessorImpl implements StyledParameterProcessor {
  @Override
  public boolean canProcess(Map<String, String> params) {
    return false;
  }

  @Override
  public Future<RequestParameter> process(Map<String, String> params) {
    return null;
  }
}

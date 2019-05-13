package io.vertx.ext.web.client.impl;

import io.vertx.ext.web.client.PathParameters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class PathParametersImpl implements PathParameters {

  Map<String, String> escapedParams;

  public PathParametersImpl() {
    this.escapedParams = new HashMap<>();
  }

  @Override
  public PathParameters param(String key, String value) {
    return escapedParam(key, urlEncode(value));
  }

  @Override
  public PathParameters escapedParam(String key, String value) {
    escapedParams.put(key, value);
    return this;
  }

  @Override
  public String getEscapedParam(String key) {
    return escapedParams.get(key);
  }

  private static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }
}

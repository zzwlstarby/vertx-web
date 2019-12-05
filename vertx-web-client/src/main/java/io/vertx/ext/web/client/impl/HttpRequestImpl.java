/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.client.impl;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpRequestImpl<T> implements HttpRequest<T> {

  final WebClientInternal client;
  final WebClientOptions options;
  SocketAddress serverAddress;
  MultiMap params;
  HttpMethod method;
  String rawMethod;
  String protocol;
  int port;
  String host;
  String virtualHost;
  String uri;
  MultiMap headers;
  long timeout = -1;
  BodyCodec<T> codec;
  boolean followRedirects;
  Boolean ssl;
  boolean multipartMixed = true;
  public List<ResponsePredicate> expectations;

  HttpRequestImpl(WebClientInternal client, HttpMethod method, SocketAddress serverAddress, Boolean ssl, int port, String host, String uri, BodyCodec<T>
          codec, WebClientOptions options) {
    this(client, method, serverAddress, null, ssl, port, host, uri, codec, options);
  }

  HttpRequestImpl(WebClientInternal client, HttpMethod method, SocketAddress serverAddress, String protocol, Boolean ssl, int port, String host, String
          uri, BodyCodec<T> codec, WebClientOptions options) {
    this.client = client;
    this.method = method;
    this.protocol = protocol;
    this.codec = codec;
    this.port = port;
    this.host = host;
    this.uri = uri;
    this.ssl = ssl;
    this.serverAddress = serverAddress;
    this.followRedirects = options.isFollowRedirects();
    this.options = options;
    if (options.isUserAgentEnabled()) {
      headers = new CaseInsensitiveHeaders().add(HttpHeaders.USER_AGENT, options.getUserAgent());
    }
  }

  private HttpRequestImpl(HttpRequestImpl<T> other) {
    this.client = other.client;
    this.serverAddress = other.serverAddress;
    this.options = other.options;
    this.method = other.method;
    this.protocol = other.protocol;
    this.port = other.port;
    this.host = other.host;
    this.timeout = other.timeout;
    this.uri = other.uri;
    this.headers = other.headers != null ? new CaseInsensitiveHeaders().addAll(other.headers) : null;
    this.params = other.params != null ? new CaseInsensitiveHeaders().addAll(other.params) : null;
    this.codec = other.codec;
    this.followRedirects = other.followRedirects;
    this.ssl = other.ssl;
    this.multipartMixed = other.multipartMixed;
  }

  @Override
  public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
    codec = (BodyCodec<T>) responseCodec;
    return (HttpRequest<U>) this;
  }

  @Override
  public HttpRequest<T> method(HttpMethod value) {
    method = value;
    return this;
  }

  public HttpMethod method() {
    return method;
  }

  @Override
  public HttpRequest<T> rawMethod(String method) {
    rawMethod = method;
    method(HttpMethod.OTHER);
    return this;
  }

  public String rawMethod() {
    return rawMethod;
  }

  @Override
  public HttpRequest<T> port(int value) {
    port = value;
    return this;
  }

  public int port() {
    return port;
  }

  @Override
  public HttpRequest<T> host(String value) {
    host = value;
    return this;
  }

  public String host() {
    return host;
  }

  @Override
  public HttpRequest<T> virtualHost(String value) {
    virtualHost = value;
    return this;
  }

  public String virtualHost() {
    return virtualHost;
  }

  @Override
  public HttpRequest<T> uri(String value) {
    params = null;
    uri = value;
    return this;
  }

  public String uri() {
    return uri;
  }

  @Override
  public HttpRequest<T> putHeaders(MultiMap headers) {
    headers().addAll(headers);
    return this;
  }

  @Override
  public HttpRequest<T> putHeader(String name, String value) {
    headers().set(name, value);
    return this;
  }

  @Override
  public HttpRequest<T> putHeader(String name, Iterable<String> value) {
    headers().set(name, value);
    return this;
  }

  @Override
  public MultiMap headers() {
    if (headers == null) {
      headers = new CaseInsensitiveHeaders();
    }
    return headers;
  }

  @Override
  public HttpRequest<T> basicAuthentication(String id, String password) {
    return this.basicAuthentication(Buffer.buffer(id), Buffer.buffer(password));
  }

  @Override
  public HttpRequest<T> basicAuthentication(Buffer id, Buffer password) {
    Buffer buff = Buffer.buffer().appendBuffer(id).appendString(":").appendBuffer(password);
    String credentials =  new String(Base64.getEncoder().encode(buff.getBytes()));
    return putHeader(HttpHeaders.AUTHORIZATION.toString(), "Basic " + credentials);
  }

  @Override
  public HttpRequest<T> bearerTokenAuthentication(String bearerToken) {
    return putHeader(HttpHeaders.AUTHORIZATION.toString(), "Bearer " + bearerToken);
  }

  @Override
  public HttpRequest<T> ssl(Boolean value) {
    ssl = value;
    return this;
  }

  public Boolean ssl() {
    return ssl;
  }

  @Override
  public HttpRequest<T> timeout(long value) {
    timeout = value;
    return this;
  }

  public long timeout() {
    return timeout;
  }

  @Override
  public HttpRequest<T> addQueryParam(String paramName, String paramValue) {
    queryParams().add(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> setQueryParam(String paramName, String paramValue) {
    queryParams().set(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> followRedirects(boolean value) {
    followRedirects = value;
    return this;
  }

  public boolean followRedirects() {
    return followRedirects;
  }

  @Override
  public HttpRequest<T> expect(ResponsePredicate expectation) {
    if (expectations == null) {
      expectations = new ArrayList<>();
    }
    expectations.add(expectation);
    return this;
  }

  @Override
  public MultiMap queryParams() {
    if (params == null) {
      params = new CaseInsensitiveHeaders();
    }
    if (params.isEmpty()) {
      int idx = uri.indexOf('?');
      if (idx >= 0) {
        QueryStringDecoder dec = new QueryStringDecoder(uri);
        dec.parameters().forEach((name, value) -> params.add(name, value));
        uri = uri.substring(0, idx);
      }
    }
    return params;
  }

  @Override
  public HttpRequest<T> copy() {
    return new HttpRequestImpl<>(this);
  }

  @Override
  public HttpRequest<T> multipartMixed(boolean allow) {
    multipartMixed = allow;
    return this;
  }

  @Override
  public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send(null, body, handler);
  }

  @Override
  public void send(Handler<AsyncResult<HttpResponse<T>>> handler) {
    send(null, null, handler);
  }

  @Override
  public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send(null, body, handler);
  }

  @Override
  public void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send("application/json", body,handler);
  }

  @Override
  public void sendJson(Object body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send("application/json", body, handler);
  }

  @Override
  public void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send("application/x-www-form-urlencoded", body, handler);
  }

  @Override
  public void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    send("multipart/form-data", body, handler);
  }
  
  private void send(String contentType, Object body, Handler<AsyncResult<HttpResponse<T>>> handler) {
    HttpContext<T> ctx = client.createContext(handler);
    ctx.prepareRequest(this, contentType, body);
  }
}

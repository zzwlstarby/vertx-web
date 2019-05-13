package io.vertx.ext.web.handler.impl;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AndChainAuthHandlerImpl extends AuthHandlerImpl implements ChainAuthHandler {

  private final List<AuthHandler> handlers = new ArrayList<>();

  public AndChainAuthHandlerImpl() {
    super(null);
  }

  @Override
  public ChainAuthHandler append(AuthHandler other) {
    handlers.add(other);
    return this;
  }

  @Override
  public boolean remove(AuthHandler other) {
    return handlers.remove(other);
  }

  @Override
  public void clear() {
    handlers.clear();
  }

  @Override
  public AuthHandler addAuthority(String authority) {
    for (AuthHandler h : handlers) {
      h.addAuthority(authority);
    }
    return this;
  }

  @Override
  public AuthHandler addAuthorities(Set<String> authorities) {
    for (AuthHandler h : handlers) {
      h.addAuthorities(authorities);
    }
    return this;
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    // Noop, we override handle
  }

  @Override
  public void handle(RoutingContext ctx) {
    iterate(0, ctx, v -> ctx.next());
  }

  private void iterate(final int idx, final RoutingContext ctx, Handler<AsyncResult<Void>> handler) {
    // stop condition
    if (idx >= handlers.size()) {
      // no more providers, means that we succeeded
      handler.handle(Future.succeededFuture());
      return;
    }

    // parse the request in order to extract the credentials object
    final AuthHandler authHandler = handlers.get(idx);

    authHandler.handle(new FakeRoutingContext(
      ctx,
      () -> iterate(idx + 1, ctx, handler),
      ctx::fail
    ));
  }

  private class FakeRoutingContext implements RoutingContext {

    final RoutingContext realRoutingContext;
    final Runnable onComplete;
    final Consumer<HttpStatusException> onFailed;

    public FakeRoutingContext(RoutingContext realRoutingContext, Runnable onComplete, Consumer<HttpStatusException> onFailed) {
      this.realRoutingContext = realRoutingContext;
      this.onComplete = onComplete;
      this.onFailed = onFailed;
    }

    public RoutingContext getRealRoutingContext() {
      return realRoutingContext;
    }

    @Override
    @CacheReturn
    public HttpServerRequest request() {
      return realRoutingContext.request();
    }

    @Override
    @CacheReturn
    public HttpServerResponse response() {
      return realRoutingContext.response();
    }

    @Override
    public void next() {
      onComplete.run();
    }

    @Override
    public void fail(int statusCode) {
      fail(statusCode, null);
    }

    @Override
    public void fail(Throwable t) {
      this.fail(-1, t);
    }

    @Override
    public void fail(int statusCode, Throwable throwable) {
      if (throwable instanceof HttpStatusException) onFailed.accept((HttpStatusException) throwable);
      else onFailed.accept(new HttpStatusException(statusCode, throwable));
    }

    @Override
    @Fluent
    public RoutingContext put(String key, Object obj) {
      return realRoutingContext.put(key, obj);
    }

    @Override
    public <T> T get(String key) {
      return realRoutingContext.get(key);
    }

    @Override
    public <T> T remove(String key) {
      return realRoutingContext.remove(key);
    }

    @Override
    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    public Map<String, Object> data() {
      return realRoutingContext.data();
    }

    @Override
    @CacheReturn
    public Vertx vertx() {
      return realRoutingContext.vertx();
    }

    @Override
    @Nullable
    public String mountPoint() {
      return realRoutingContext.mountPoint();
    }

    @Override
    public Route currentRoute() {
      return realRoutingContext.currentRoute();
    }

    @Override
    public String normalisedPath() {
      return realRoutingContext.normalisedPath();
    }

    @Override
    public @Nullable Cookie getCookie(String name) {
      return realRoutingContext.getCookie(name);
    }

    @Override
    @Fluent
    public RoutingContext addCookie(Cookie cookie) {
      return realRoutingContext.addCookie(cookie);
    }

    @Override
    public @Nullable Cookie removeCookie(String name) {
      return realRoutingContext.removeCookie(name);
    }

    @Override
    public @Nullable Cookie removeCookie(String name, boolean invalidate) {
      return realRoutingContext.removeCookie(name, invalidate);
    }

    @Override
    public int cookieCount() {
      return realRoutingContext.cookieCount();
    }

    @Override
    public Set<Cookie> cookies() {
      return realRoutingContext.cookies();
    }

    @Override
    @Nullable
    public String getBodyAsString() {
      return realRoutingContext.getBodyAsString();
    }

    @Override
    @Nullable
    public String getBodyAsString(String encoding) {
      return realRoutingContext.getBodyAsString(encoding);
    }

    @Override
    @Nullable
    public JsonObject getBodyAsJson() {
      return realRoutingContext.getBodyAsJson();
    }

    @Override
    public @Nullable JsonArray getBodyAsJsonArray() {
      return realRoutingContext.getBodyAsJsonArray();
    }

    @Override
    public @Nullable Buffer getBody() {
      return realRoutingContext.getBody();
    }

    @Override
    public Set<FileUpload> fileUploads() {
      return realRoutingContext.fileUploads();
    }

    @Override
    public @Nullable Session session() {
      return realRoutingContext.session();
    }

    @Override
    public @Nullable User user() {
      return realRoutingContext.user();
    }

    @Override
    @CacheReturn
    @Nullable
    public Throwable failure() {
      return realRoutingContext.failure();
    }

    @Override
    @CacheReturn
    public int statusCode() {
      return realRoutingContext.statusCode();
    }

    @Override
    @Nullable
    public String getAcceptableContentType() {
      return realRoutingContext.getAcceptableContentType();
    }

    @Override
    @CacheReturn
    public ParsedHeaderValues parsedHeaders() {
      return realRoutingContext.parsedHeaders();
    }

    @Override
    public int addHeadersEndHandler(Handler<Void> handler) {
      return realRoutingContext.addHeadersEndHandler(handler);
    }

    @Override
    public boolean removeHeadersEndHandler(int handlerID) {
      return realRoutingContext.removeHeadersEndHandler(handlerID);
    }

    @Override
    public int addBodyEndHandler(Handler<Void> handler) {
      return realRoutingContext.addBodyEndHandler(handler);
    }

    @Override
    public boolean removeBodyEndHandler(int handlerID) {
      return realRoutingContext.removeBodyEndHandler(handlerID);
    }

    @Override
    public boolean failed() {
      return realRoutingContext.failed();
    }

    @Override
    public void setBody(Buffer body) {
      realRoutingContext.setBody(body);
    }

    @Override
    public void setSession(Session session) {
      realRoutingContext.setSession(session);
    }

    @Override
    public void setUser(User user) {
      realRoutingContext.setUser(user);
    }

    @Override
    public void clearUser() {
      realRoutingContext.clearUser();
    }

    @Override
    public void setAcceptableContentType(@Nullable String contentType) {
      realRoutingContext.setAcceptableContentType(contentType);
    }

    @Override
    public void reroute(String path) {
      realRoutingContext.reroute(path);
    }

    @Override
    public void reroute(HttpMethod method, String path) {
      realRoutingContext.reroute(method, path);
    }

    @Override
    @CacheReturn
    @Deprecated
    public List<Locale> acceptableLocales() {
      return realRoutingContext.acceptableLocales();
    }

    @Override
    @CacheReturn
    public List<LanguageHeader> acceptableLanguages() {
      return realRoutingContext.acceptableLanguages();
    }

    @Override
    @Deprecated
    @CacheReturn
    public Locale preferredLocale() {
      return realRoutingContext.preferredLocale();
    }

    @Override
    @CacheReturn
    public LanguageHeader preferredLanguage() {
      return realRoutingContext.preferredLanguage();
    }

    @Override
    public Map<String, String> pathParams() {
      return realRoutingContext.pathParams();
    }

    @Override
    @Nullable
    public String pathParam(String name) {
      return realRoutingContext.pathParam(name);
    }

    @Override
    public MultiMap queryParams() {
      return realRoutingContext.queryParams();
    }

    @Override
    @Nullable
    public List<String> queryParam(String query) {
      return realRoutingContext.queryParam(query);
    }
  }
}

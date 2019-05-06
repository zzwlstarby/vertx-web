package io.vertx.ext.web.api.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.service.impl.RouteToEBServiceHandlerImpl;

import java.util.function.Function;

@VertxGen
public interface RouteToEBServiceHandler extends Handler<RoutingContext> {

  @Fluent
  RouteToEBServiceHandlerImpl extraPayloadMapper(Function<RoutingContext, JsonObject> extraPayloadMapper);

  static RouteToEBServiceHandlerImpl build(EventBus eventBus, String address, String actionName) {
    return new RouteToEBServiceHandlerImpl(eventBus, address, new DeliveryOptions().addHeader("action", actionName));
  }

  static RouteToEBServiceHandlerImpl build(EventBus eventBus, String address, String actionName, DeliveryOptions deliveryOptions) {
    return new RouteToEBServiceHandlerImpl(eventBus, address, new DeliveryOptions(deliveryOptions).addHeader("action", actionName));
  }
}

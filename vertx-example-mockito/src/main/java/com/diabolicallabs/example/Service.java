package com.diabolicallabs.example;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface Service {

  /**
   * This is the default address that the service proxy will listen to for events on the Event Bus
   */
  String DEFAULT_ADDRESS = "service.example";

  /**
   * Creates a service instance
   * @return An instance of the default service implementation
   */
  @GenIgnore
  static Service create() {
    return new ServiceImpl();
  }

  /**
   * Creates a proxy for the service that listens for events on the Event Bus
   * @param vertx An instance of Vertx
   * @param address This is the address the service will listen to on the Event Bus
   * @return An instance fo the service proxy
   */
  @GenIgnore
  static Service createProxy(Vertx vertx, String address) {
    return new ServiceVertxEBProxy(vertx, address);
  }

  /**
   * Repeats the text passed by the caller
   * @param text The text to repeat
   * @param handler This handler will be called with a copy of the text passed
   */
  void repeat(String text, Handler<AsyncResult<String>> handler);

}

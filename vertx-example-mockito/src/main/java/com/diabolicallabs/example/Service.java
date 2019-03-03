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

  String DEFAULT_ADDRESS = "service.example";

  @GenIgnore
  static Service create() {
    return new ServiceImpl();
  }

  @GenIgnore
  static Service createProxy(Vertx vertx, String address) {
    return new ServiceVertxEBProxy(vertx, address);
  }

  void repeat(String text, Handler<AsyncResult<String>> handler);
  void reverse(String text, Handler<AsyncResult<String>> handler);

}

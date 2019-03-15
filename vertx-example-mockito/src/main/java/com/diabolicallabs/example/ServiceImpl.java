package com.diabolicallabs.example;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class ServiceImpl implements Service {

  @Override
  public void repeat(String text, Handler<AsyncResult<String>> handler) {
    handler.handle(Future.succeededFuture(text));
  }

}

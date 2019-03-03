package com.diabolicallabs.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;

public class Verticle extends AbstractVerticle {

  private Service exampleService;
  private Service exampleServiceProxy;

  @Override
  public void init(Vertx vertx, Context context) {

    exampleService = Service.create();
    exampleServiceProxy = Service.createProxy(vertx, Service.DEFAULT_ADDRESS);
    super.init(vertx, context);

  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    new ServiceBinder(vertx)
      .setAddress(Service.DEFAULT_ADDRESS)
      .register(Service.class, new ServiceImpl());

    vertx.eventBus().<String>consumer("example.service.repeat", handler -> {
      String text = handler.body();
      exampleService.repeat(text, stringAsyncResult -> {
        if (stringAsyncResult.succeeded()) {
          handler.reply(stringAsyncResult.result());
        } else {
          handler.fail(-1, stringAsyncResult.cause().getMessage());
        }
      });
    });

    vertx.eventBus().<String>consumer("example.service.proxy.repeat", handler -> {
      String text = handler.body();
      exampleServiceProxy.repeat(text, stringAsyncResult -> {
        if (stringAsyncResult.succeeded()) {
          handler.reply(stringAsyncResult.result());
        } else {
          handler.fail(-1, stringAsyncResult.cause().getMessage());
        }
      });
    });

    startFuture.complete();
  }

  public Verticle setExampleService(Service exampleService) {
    this.exampleService = exampleService;
    return this;
  }

  public Verticle setExampleServiceProxy(Service exampleServiceProxy) {
    this.exampleServiceProxy = exampleServiceProxy;
    return this;
  }
}

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

    vertx.eventBus().<String>consumer("example.service", handler -> {
      String text = handler.body();
      exampleService.repeat(text, stringAsyncResult -> {
        handler.reply(stringAsyncResult.result());
      });
    });

    vertx.eventBus().<String>consumer("example.service.proxy", handler -> {
      String text = handler.body();
      exampleServiceProxy.repeat(text, stringAsyncResult -> {
        handler.reply(stringAsyncResult.result());
      });
    });

    startFuture.complete();
  }

  public Verticle setExampleService(Service exampleService) {
    this.exampleService = exampleService;
    System.out.println("Finished with setExampleService");
    return this;
  }

  public Verticle setExampleServiceProxy(Service exampleServiceProxy) {
    this.exampleServiceProxy = exampleServiceProxy;
    System.out.println("Finished with setExampleServiceProxy");
    return this;
  }
}

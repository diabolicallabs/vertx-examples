package com.diabolicallabs.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;

public class Verticle extends AbstractVerticle {

  private Service exampleService;       //Default implementation of Service
  private Service exampleServiceProxy;  //A proxy to the default implementation of Service

  @Override
  public void init(Vertx vertx, Context context) {

    //Create an implementation of Service
    exampleService = Service.create();
    //Create a proxy to an implementation of Service
    exampleServiceProxy = Service.createProxy(vertx, Service.DEFAULT_ADDRESS);

    super.init(vertx, context);

  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    //Bind the default implementation of Service to the Event Bus at the default address
    new ServiceBinder(vertx)
      .setAddress(Service.DEFAULT_ADDRESS)
      .register(Service.class, new ServiceImpl());

    //Create a consumer that will call the repeat method of the service and reply with the result
    //This allows us to demonstrate how a service can be mocked during testing
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

    //Create a consumer that will call the repeat method of the service proxy and reply with the result
    //This allows us to demonstrate how a proxy to a service can be mocked during testing
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

}

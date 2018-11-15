package com.diabolicallabs.vertx.hazelcast;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;

import java.time.Instant;

public class Verticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    String sendAddress = System.getenv("SEND_ADDRESS");
    String receiveAddress = System.getenv("RECEIVE_ADDRESS");

    vertx.setPeriodic(1000 * 10, handler -> {
      vertx.eventBus().<String>send(sendAddress, Instant.now().toString(), replyHandler -> {
        if (replyHandler.succeeded()) {
          Message<String> message = replyHandler.result();
          if (message != null) {
            System.out.println("Got a reply of: " + message.body());
          }
        } else {
          System.out.println("Unable to receive reply " + replyHandler.cause().getMessage());
        }
      });
    });

    vertx.<String>eventBus().consumer(receiveAddress, handler -> {
      System.out.println("Received: " + handler.body() + " on " + receiveAddress);
      handler.reply("Mahalo plenty for sending: " + handler.body());
    });
  }
}

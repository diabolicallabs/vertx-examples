package com.diabolicallabs.examples;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MongoDateExampleVerticle extends AbstractVerticle {

  public static String COLLECTION_NAME = "DATETEST";

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    AtomicInteger count = new AtomicInteger(2);

    MongoClient mongoClient = MongoClient.createShared(vertx, config());

    getVertx().eventBus().consumer("person.save", handler -> {

      if (handler.body() == null || !(handler.body() instanceof JsonObject)) throw new IllegalArgumentException("Message must include a JSON object");

      JsonObject person = (JsonObject) handler.body();

      if (!person.containsKey("name")) throw  new IllegalArgumentException("Message must include 'name'");
      if (!person.containsKey("birthDate")) throw  new IllegalArgumentException("Message must include 'birthDate'");

      JsonObject document = new JsonObject();
      document.put("name", person.getString("name"));
      /*
      Mongo needs to know that 'birthDate' is a date field. Otherwise, it will save the date as a string.
      The '$date' directive tells mongo to save the field as an ISO Date.
       */
      document.put("birthDate", new JsonObject().put("$date", person.getString("birthDate")));

      mongoClient.save(COLLECTION_NAME, document, mongoHandler -> {
        if (mongoHandler.succeeded()) handler.reply(mongoHandler.result());
        else if (mongoHandler.failed()) handler.fail(-1, mongoHandler.cause().getMessage());
      });

    }).completionHandler( handler -> {
      if (handler.succeeded()) {
        if (count.decrementAndGet() == 0) startFuture.complete();
      }
      else if (handler.failed()) startFuture.fail(handler.cause());
    });

    getVertx().eventBus().consumer("person.read", handler -> {

      if (handler.body() == null || !(handler.body() instanceof JsonObject)) throw new IllegalArgumentException("Message must include a JSON object");

      JsonObject person = (JsonObject) handler.body();

      if (!person.containsKey("name")) throw  new IllegalArgumentException("Message must include 'name'");

      JsonObject query = new JsonObject().put("name", person.getString("name"));

      mongoClient.findOne(COLLECTION_NAME, query, null, mongoHandler -> {
        if (mongoHandler.succeeded()) {

          JsonObject found = mongoHandler.result();

          JsonObject reply = new JsonObject().put("name", found.getString("name"));
          if (found.containsKey("birthDate")) {
            /*
            Since 'birthDate' was saved using the '$date' directive, we need to get the string
            value from the '$date' field and place it in the 'birthDate' field. This will hide
            Mongo specific directives from the client of the 'person.read' message.
             */
            reply.put("birthDate", found.getJsonObject("birthDate").getString("$date"));
          }

          handler.reply(reply);
        } else if (mongoHandler.failed()) handler.fail(-1, mongoHandler.cause().getMessage());
      });

    }).completionHandler( handler -> {
      if (handler.succeeded()) {
        if (count.decrementAndGet() == 0) startFuture.complete();
      }
      else if (handler.failed()) startFuture.fail(handler.cause());
    });

  }
}

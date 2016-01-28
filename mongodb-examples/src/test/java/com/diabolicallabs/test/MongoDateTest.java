package com.diabolicallabs.test;

import com.diabolicallabs.examples.MongoDateExampleVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class MongoDateTest extends MongoTestBase {

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Before
  public void before(TestContext context) {

    rule.vertx().deployVerticle(MongoDateExampleVerticle.class.getName(), context.asyncAssertSuccess(id -> {
      System.out.println("MongoDateExampleVerticle deployment id: " + id);
      MongoClient mongoClient = MongoClient.createShared((rule.vertx()), new JsonObject().put("port", 27018));
      context.put("mongoClient", mongoClient);
    }));

  }

  @Test
  public void testCreateDate(TestContext context) {

    Async async = context.async();

    MongoClient mongoClient = context.get("mongoClient");

    mongoClient.dropCollection(MongoDateExampleVerticle.COLLECTION_NAME, mongoHandler -> {

      context.assertTrue(mongoHandler.succeeded());
      context.assertFalse(mongoHandler.failed());

      JsonObject person = new JsonObject()
        .put("name", "Alan Turing")
        .put("birthDate", "1912-06-23T16:07:37Z");

      rule.vertx().eventBus().send("person.save", person, handler -> {
        context.assertTrue(handler.succeeded());
        context.assertFalse(handler.failed());
        context.assertNotNull(handler.result());

        mongoClient.count(MongoDateExampleVerticle.COLLECTION_NAME, new JsonObject(), countHandler -> {
          context.assertTrue(countHandler.succeeded());
          context.assertFalse(countHandler.failed());
          context.assertEquals(1L, countHandler.result());
          async.complete();
        });
      });
    });

  }
  @Test
  public void testReadDate(TestContext context) {

    Async async = context.async();

    MongoClient mongoClient = context.get("mongoClient");

    mongoClient.dropCollection(MongoDateExampleVerticle.COLLECTION_NAME, mongoHandler -> {

      context.assertTrue(mongoHandler.succeeded());
      context.assertFalse(mongoHandler.failed());

      JsonObject person = new JsonObject()
        .put("name", "Marvin Minsky")
        .put("birthDate", "1927-08-09T18:22:12Z");

      rule.vertx().eventBus().send("person.save", person, handler -> {
        context.assertTrue(handler.succeeded());
        context.assertFalse(handler.failed());
        context.assertNotNull(handler.result());

        rule.vertx().eventBus().send("person.read", new JsonObject().put("name", "Marvin Minsky"), readHandler -> {
          context.assertTrue(readHandler.succeeded());
          context.assertFalse(readHandler.failed());
          context.assertNotNull(readHandler.result());

          JsonObject result = (JsonObject) readHandler.result().body();
          context.assertEquals("Marvin Minsky", result.getString("name"));
          context.assertEquals("1927-08-09T18:22:12Z", result.getString("birthDate"));

          async.complete();
        });
      });
    });

  }
}

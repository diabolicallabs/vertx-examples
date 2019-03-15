package com.diabolicallabs.example.test;

import com.diabolicallabs.example.Service;
import com.diabolicallabs.example.Verticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;

@RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class ServiceTest {

  @Mock
  private Service exampleService;

  @InjectMocks
  Verticle exampleVerticle;

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void before(TestContext context) {

    Async async = context.async();

    rule.vertx().deployVerticle(exampleVerticle, stringAsyncResult -> {

      /*
       * Mockito will inject the mocks into the Verticle when it is created. Unfortunately,
       * Vert.x will call the init and start methods after that when the Verticle is
       * deployed. So, we need to re-set the services back to the mocks after deployment
       * with reflection.
       */
      try {
        Field exampleServiceProxyField = Verticle.class.getDeclaredField("exampleService");
        exampleServiceProxyField.setAccessible(true);
        exampleServiceProxyField.set(exampleVerticle, exampleService);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      Mockito.doAnswer((Answer<Void>) invocation -> {
        String parameter = invocation.getArgument(0);
        Handler<AsyncResult<String>> handler = (Handler<AsyncResult<String>>) invocation.getArguments()[1];

        System.out.println("Mocked 'repeat' method received parameter: " + parameter);

        switch (parameter) {
          case "goat":
            handler.handle(Future.succeededFuture("kao"));
            break;
          case "eel":
            handler.handle(Future.succeededFuture("puhi"));
            break;
          default:
            handler.handle(Future.failedFuture(new RuntimeException(parameter)));
        }
        return null;
      }).when(exampleService).repeat(Mockito.any(String.class), Mockito.any(Handler.class));

      async.complete();
    });

  }

  @Test
  public void testMockedServiceMethodWithGoat(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.repeat", "goat", result -> {
      context.assertTrue(result.succeeded());
      //The normal service just repeats the parameter back as a reply. The mocked service
      //will return the parameter translated to Hawaiian. If we get back Hawaiian, then
      //the mocked service was called.
      context.assertEquals("kao", result.result().body());
      async.complete();
    });
  }

  @Test
  public void testMockedServiceMethodWithEel(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.repeat", "eel", result -> {
      context.assertTrue(result.succeeded());
      //The normal service just repeats the parameter back as a reply. The mocked service
      //will return the parameter translated to Hawaiian. If we get back Hawaiian, then
      //the mocked service was called.
      context.assertEquals("puhi", result.result().body());
      async.complete();
    });
  }

  @Test
  public void testMockedServiceMethodWithFailure(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.repeat", "mongoose", result -> {
      //"mongoose" was not one of the parameters we accounted for in the mocked service.
      //In this case the mocked service should return a failure.
      context.assertFalse(result.succeeded());
      context.assertEquals("mongoose", result.cause().getMessage());
      async.complete();
    });
  }

  @Test
  public void testNativeProxyServiceMethodWithGoat(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy.repeat", "goat", result -> {
      //In this test case we are trying the proxied service which is not mocked.
      //We should get back the parameter unchanged.
      context.assertTrue(result.succeeded());
      context.assertEquals("goat", result.result().body());
      async.complete();
    });
  }
}

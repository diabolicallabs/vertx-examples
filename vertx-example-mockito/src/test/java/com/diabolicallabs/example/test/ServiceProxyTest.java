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

@RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class ServiceProxyTest {

  @Mock
  private Service exampleServiceProxy;

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
      exampleVerticle.setExampleServiceProxy(exampleServiceProxy);

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
      }).when(exampleServiceProxy).repeat(Mockito.any(String.class), Mockito.any(Handler.class));

      async.complete();
    });

  }

  @Test
  public void testMockedServiceProxyMethodWithGoat(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy.repeat", "goat", result -> {
      context.assertTrue(result.succeeded());
      context.assertEquals("kao", result.result().body());
      async.complete();
    });
  }

  @Test
  public void testMockedServiceProxyMethodWithEel(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy.repeat", "eel", result -> {
      context.assertTrue(result.succeeded());
      context.assertEquals("puhi", result.result().body());
      async.complete();
    });
  }

  @Test
  public void testMockedServiceProxyMethodWithFailure(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy.repeat", "mongoose", result -> {
      context.assertFalse(result.succeeded());
      context.assertEquals("mongoose", result.cause().getMessage());
      async.complete();
    });
  }


  @Test
  public void testNativeServiceMethodWithGoat(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.repeat", "goat", result -> {
      context.assertTrue(result.succeeded());
      context.assertEquals("goat", result.result().body());
      async.complete();
    });
  }
}

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

  @Mock private Service exampleServiceProxy;

  @InjectMocks
  Verticle exampleVerticle;

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void before(TestContext context) {

    Async async = context.async();

    rule.vertx().deployVerticle(exampleVerticle, stringAsyncResult -> {
      exampleVerticle.setExampleServiceProxy(exampleServiceProxy);
      async.complete();
    });

  }

  @Test
  public void testService(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy", "goat", result -> {
      context.assertTrue(result.succeeded());
      context.assertEquals("mock", result.result().body());
      async.complete();
    });

  }

  @Test
  public void testMockedService(TestContext context) {

    Mockito.doAnswer((Answer<Void>) invocation -> {
      System.out.println("mocking");
      Handler<AsyncResult<String>> handler = (Handler<AsyncResult<String>>) invocation.getArguments()[1];
      handler.handle(Future.succeededFuture("mock"));
      return null;
    }).when(exampleServiceProxy).repeat(Mockito.any(String.class), Mockito.any(Handler.class));

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy", "goat", result -> {
      context.assertTrue(result.succeeded());
      context.assertEquals("mock", result.result().body());
      async.complete();
    });
  }
}

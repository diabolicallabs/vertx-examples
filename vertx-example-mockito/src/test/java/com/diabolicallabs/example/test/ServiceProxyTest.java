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
public class ServiceProxyTest {

  /**
   * This is the service we want to mock with Mockito. The name must match the variable
   * name in the Verticle
   */
  @Mock
  private Service exampleServiceProxy;

  /**
   * This is the Verticle that we want to inject the mock service into
   */
  @InjectMocks
  Verticle exampleVerticle;

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  /**
   * Adding this Mocktio rule will allow mockito to run along with the
   * Vert.x Junit runner
   */
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  /**
   * This method will be called before each test. We will deploy a clean Verticle each
   * time and once it is deployed, set up the mock service and set it in the Verticle.
   * @param context The test context
   */
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
        Field exampleServiceProxyField = Verticle.class.getDeclaredField("exampleServiceProxy");
        exampleServiceProxyField.setAccessible(true);
        exampleServiceProxyField.set(exampleVerticle, exampleServiceProxy);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      //This will be called by Mockito whenever the repeat method is invoked
      Mockito.doAnswer((Answer<Void>) invocation -> {
        //Grab the first argument of the repeat method invocation
        String parameter = invocation.getArgument(0);
        //Grab the second argument which is the handler we will call back with the result of the invocation
        Handler<AsyncResult<String>> handler = (Handler<AsyncResult<String>>) invocation.getArguments()[1];

        System.out.println("Mocked 'repeat' method received parameter: " + parameter);

        //Call the result handler with different values based on the parameter
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
        //The return value of the repeat method is null. We are returning the value by calling the async
        //result handler in the above switch statement.
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
      //The normal service just repeats the parameter back as a reply. The mocked service
      //will return the parameter translated to Hawaiian. If we get back Hawaiian, then
      //the mocked service was called.
      context.assertEquals("kao", result.result().body());
      async.complete();
    });
  }

  @Test
  public void testMockedServiceProxyMethodWithEel(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy.repeat", "eel", result -> {
      context.assertTrue(result.succeeded());
      //The normal service just repeats the parameter back as a reply. The mocked service
      //will return the parameter translated to Hawaiian. If we get back Hawaiian, then
      //the mocked service was called.
      context.assertEquals("puhi", result.result().body());
      async.complete();
    });
  }

  @Test
  public void testMockedServiceProxyMethodWithFailure(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.proxy.repeat", "mongoose", result -> {
      //"mongoose" was not one of the parameters we accounted for in the mocked service.
      //In this case the mocked service should return a failure.
      context.assertFalse(result.succeeded());
      context.assertEquals("mongoose", result.cause().getMessage());
      async.complete();
    });
  }


  @Test
  public void testNativeServiceMethodWithGoat(TestContext context) {

    Async async = context.async();
    rule.vertx().eventBus().send("example.service.repeat", "goat", result -> {
      //In this test case we are trying the non-proxied service which is not mocked.
      //We should get back the parameter unchanged.
      context.assertTrue(result.succeeded());
      context.assertEquals("goat", result.result().body());
      async.complete();
    });
  }
}

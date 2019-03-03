package com.diabolicallabs.example.test;

import com.diabolicallabs.example.Service;
import com.diabolicallabs.example.Verticle;
import io.vertx.ext.unit.junit.RunTestOnContext;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class ServiceTest {

  @Mock
  private Service exampleService;

  @InjectMocks
  Verticle exampleVerticle;

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
}

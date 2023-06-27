package io.resys.thena.tasks.tests.config;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import com.fasterxml.jackson.databind.ObjectMapper;

@Dependent
public class BeanFactoryForTests {
  
  @Produces
  public ObjectMapper objectMapper() {
    return TaskTestCase.objectMapper();
  }
}
package io.resys.thena.tasks.spi.store;

public class MainBranch {

  public static String HEAD_NAME = "main";
  
  public static boolean isMain(String tagName) {
    return tagName.equals(HEAD_NAME);
  }
}

package io.resys.thena.docdb.spi.pgsql;

import java.util.ArrayList;
import java.util.Arrays;

import io.resys.thena.docdb.spi.ErrorHandler;
import io.vertx.pgclient.PgException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgErrors implements ErrorHandler {
  
  public boolean notFound(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      
      return "42P01".equals(ogre.getCode());
    }
    return false;
  }
  
  public boolean duplicate(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      
      return "23505".equals(ogre.getCode());
    }
    return false;
  }
  
  @Override
  public boolean isLocked(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      return "55P03".equals(ogre.getCode());
    }
    return false;
  }
  
  public void deadEnd(String additionalMsg, Throwable e) {
    log.error(additionalMsg + System.lineSeparator() + e.getMessage(), e);
  }
  
  public void deadEnd(String additionalMsg) {
    log.error(additionalMsg);
  }

  @Override
  public void deadEnd(String additionalMsg, Throwable e, Object... args) {
    final var allArgs = new ArrayList<>(Arrays.asList(args));
    allArgs.add(e);
    log.error(additionalMsg + System.lineSeparator() + e.getMessage(), allArgs.toArray()); 
  }
  
  
}

package io.resys.thena.docdb.spi.commits.body;

import io.resys.thena.docdb.api.LogConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_COMMIT)
public class CommitLogger {
  private final StringBuilder data = new StringBuilder();
  
  public CommitLogger append(String data) {
    if(log.isDebugEnabled()) {
      this.data.append(data);
    }
    return this;
  }
  @Override
  public String toString() {
    if(log.isDebugEnabled()) {
      log.debug(data.toString());
    } else {
      data.append("Log DEBUG disabled for: " + CommitBodyVisitor.class.getCanonicalName() + "!");
    }
    return data.toString();
  }
} 

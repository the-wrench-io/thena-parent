package io.resys.thena.tasks.spi.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObjects;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.vertx.core.json.JsonObject;



public class DocumentStoreException extends RuntimeException {

  private static final long serialVersionUID = 7058468238867536222L;

  private final String code;
  private final JsonObject target;
  private final List<DocumentExceptionMsg> messages = new ArrayList<>();
  
  @Value.Immutable @JsonSerialize(as = ImmutableDocumentExceptionMsg.class)
  public interface DocumentExceptionMsg {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  
  public DocumentStoreException(String code, DocumentExceptionMsg ... msg) {
    super(new ExMessageFormatter(code, null, msg).format());
    this.code = code;
    this.messages.addAll(Arrays.asList(msg));
    this.target = null;
  }
  public DocumentStoreException(String code, JsonObject target, DocumentExceptionMsg ... msg) {
    super(new ExMessageFormatter(code, target, msg).format());
    this.code = code;
    this.messages.addAll(Arrays.asList(msg));
    this.target = target;
  }

  public String getCode() { return code; }
  public List<DocumentExceptionMsg> getMessages() { return messages; }
  public JsonObject getTarget() { return target; }  
  
 
  public static DocumentExceptionMsg convertMessages(CommitResult commit) {
    return ImmutableDocumentExceptionMsg.builder()
        .id(commit.getGid())
        .value("") //TODO
        .addAllArgs(commit.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  public static DocumentExceptionMsg convertMessages1(ObjectsResult<BlobObject> state) {
    return ImmutableDocumentExceptionMsg.builder()
        .id("STATE_FAIL")
        .value("")
        .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }
  public static DocumentExceptionMsg convertMessages2(ObjectsResult<BlobObjects> state) {
    return ImmutableDocumentExceptionMsg.builder()
        .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

}

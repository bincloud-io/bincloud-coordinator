package io.bcs.domain.model.file;

import io.bce.promises.Promise;

public interface ContentReceiver {
  public Promise<Void> receiveFullContent(FileContent content);

  public Promise<Void> receiveContentRange(FileContent content);

  public Promise<Void> receiveContentRanges(FileContent content);
}

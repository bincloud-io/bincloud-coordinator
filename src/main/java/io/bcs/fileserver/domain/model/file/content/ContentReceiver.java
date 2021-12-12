package io.bcs.fileserver.domain.model.file.content;

import io.bce.promises.Promise;

/**
 * This interface describes the component, receiving a file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ContentReceiver {
  Promise<Void> receiveFullContent(FileContent content);

  Promise<Void> receiveContentRange(FileContent content);

  Promise<Void> receiveContentRanges(FileContent content);
}

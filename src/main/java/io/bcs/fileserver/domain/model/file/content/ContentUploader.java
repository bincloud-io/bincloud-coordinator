package io.bcs.fileserver.domain.model.file.content;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import lombok.RequiredArgsConstructor;

/**
 * This interface describes the component, uploading file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ContentUploader {
  private final FileStorage fileStorage;
  private final ContentSource contentSender;

  /**
   * Upload content to the file on the storage.
   *
   * @param file The file
   * @return The promise of uploaded content statistic
   */
  public Promise<FileUploadStatistic> upload(File file) {
    return Promises.of(deferred -> {
      Destination<BinaryChunk> destination = fileStorage.getAccessOnWrite(file.getLocator());
      contentSender.sendContent(file.getLocator(), destination).delegate(deferred);
    });
  }

  /**
   * This interface describes the component, sending file content to the storage.
   *
   * @author Dmitry Mikhaylenko
   */
  public interface ContentSource {
    /**
     * Send content to the file.
     *
     * @param contentLocator The content locator
     * @param destination    The content destination
     * @return The promise of uploaded content statistic
     */
    Promise<FileUploadStatistic> sendContent(ContentLocator contentLocator,
        Destination<BinaryChunk> destination);
  }
}

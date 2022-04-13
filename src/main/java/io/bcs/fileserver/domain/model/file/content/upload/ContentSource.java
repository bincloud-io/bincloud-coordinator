package io.bcs.fileserver.domain.model.file.content.upload;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.storage.ContentLocator;

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
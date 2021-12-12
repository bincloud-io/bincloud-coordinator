package io.bcs.fileserver.domain.model.file.content;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.FileUploadStatistic;
import io.bcs.fileserver.domain.model.storage.ContentLocator;

/**
 * This interface describes the component, uploading file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ContentUploader {
  Promise<FileUploadStatistic> upload(ContentLocator locator,
      Destination<BinaryChunk> destination);
}

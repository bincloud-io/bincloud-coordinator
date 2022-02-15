package io.bcs.fileserver.domain.model.storage;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.file.File;

/**
 * This interface describes the file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileStorage {
  default ContentLocator create(File file, Long contentLength) throws FileStorageException {
    throw new FileStorageException(
        new UnsupportedOperationException("Operation not supported for current storage type."));
  }

  default Destination<BinaryChunk> getAccessOnWrite(File file)
      throws FileStorageException {
    throw new FileStorageException(
        new UnsupportedOperationException("Operation not supported for current storage type."));
  }

  default Source<BinaryChunk> getAccessOnRead(File file,
      ContentFragment fragment) throws FileStorageException {
    throw new FileStorageException(
        new UnsupportedOperationException("Operation not supported for current storage type."));
  }

  default void delete(File file) throws FileStorageException {
    throw new FileStorageException(
        new UnsupportedOperationException("Operation not supported for current storage type."));
  }

}

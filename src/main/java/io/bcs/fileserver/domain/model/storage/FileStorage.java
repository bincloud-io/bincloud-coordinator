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
  ContentLocator create(File file, Long contentLength) throws FileStorageException;

  Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
      throws FileStorageException;

  Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator, ContentFragment fragment)
      throws FileStorageException;

  void delete(File file) throws FileStorageException;
}

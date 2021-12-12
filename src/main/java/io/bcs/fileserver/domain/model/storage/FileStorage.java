package io.bcs.fileserver.domain.model.storage;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.errors.FileStorageException;

/**
 * This interface describes the file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileStorage {
  public ContentLocator create(String mediaType) throws FileStorageException;

  public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
      throws FileStorageException;

  public Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator,
      ContentFragment fragment) throws FileStorageException;

  public void delete(ContentLocator contentLocator) throws FileStorageException;

}

package io.bcs.fileserver.infrastructure.storage;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;

/**
 * This class implements the file storage proxy, allowing to get access to the file content, located
 * on the remote distribution point.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class RemoteDistributionPointStorage implements FileStorage {

  @Override
  public ContentLocator create(String mediaType) throws FileStorageException {
    throw new FileStorageException(new UnsupportedOperationException());
  }

  @Override
  public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
      throws FileStorageException {
    throw new FileStorageException(new UnsupportedOperationException());
  }

  @Override
  public Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator,
      ContentFragment fragment) throws FileStorageException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void delete(ContentLocator contentLocator) throws FileStorageException {
    throw new FileStorageException(new UnsupportedOperationException());
  }
}

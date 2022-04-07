package io.bcs.fileserver.domain.services;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.StorageDescriptor;
import io.bcs.fileserver.domain.model.storage.StorageDescriptorRepository;
import lombok.RequiredArgsConstructor;

/**
 * This service is the adapter to concrete file storage implementation.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ContentAccessService implements FileStorage {
  private final StorageDescriptorRepository storageDescriptorRepositoy;

  @Override
  public ContentLocator create(FileDescriptor file, Long contentLength)
      throws FileStorageException {
    return findExistingStorageDescriptor(file).getStorage().create(file, contentLength);
  }

  @Override
  public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
      throws FileStorageException {
    return findExistingStorageDescriptor(contentLocator).getStorage()
        .getAccessOnWrite(contentLocator);
  }

  @Override
  public Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator,
      ContentFragment fragment) throws FileStorageException {
    return findExistingStorageDescriptor(contentLocator).getStorage()
        .getAccessOnRead(contentLocator, fragment);
  }

  @Override
  public void delete(FileDescriptor file) throws FileStorageException {
    findExistingStorageDescriptor(file).getStorage().delete(file);
  }

  private StorageDescriptor findExistingStorageDescriptor(FileDescriptor file) {
    return findExistingStorageDescriptor(file.getStorageName().get());
  }

  private StorageDescriptor findExistingStorageDescriptor(ContentLocator contentLocator) {
    return findExistingStorageDescriptor(contentLocator.getStorageName());
  }

  private StorageDescriptor findExistingStorageDescriptor(String storageName) {
    return storageDescriptorRepositoy.findStorageDescriptor(storageName).get();
  }
}

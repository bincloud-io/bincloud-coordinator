package io.bcs.fileserver.infrastructure.storage;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.DefaultContentLocator;
import io.bcs.fileserver.domain.model.storage.FileContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptor;
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptorRepository;
import io.bcs.fileserver.infrastructure.storage.PhysicalFile.Factory;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the file storage, which stores.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class LocalFileSystemStorage implements FileStorage {
  private final FilesystemSpaceManager filesystemSpaceManager;
  private final LocalStorageDescriptorRepository localStorageDescriptorRepository;
  private final Factory physicalFileFactory;
  private final int bufferSize;

  @Override
  public ContentLocator create(File file, Long contentLength) throws FileStorageException {
    try {
      String storageName = filesystemSpaceManager.allocateSpace(file.getMediaType(),
          file.getStorageFileName(), contentLength);
      ContentLocator contentLocator =
          new DefaultContentLocator(storageName, file.getStorageFileName());
      getPhysicalFile(contentLocator).create();
      return contentLocator;
    } catch (IOException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
      throws FileStorageException {
    try {
      return getPhysicalFile(contentLocator).openForWrite();
    } catch (IOException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator,
      ContentFragment fragment) throws FileStorageException {
    try {
      return getPhysicalFile(contentLocator).openForRead(fragment.getOffset(), fragment.getLength(),
          bufferSize);
    } catch (IOException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public void delete(File file) throws FileStorageException {
    getPhysicalFile(new FileContentLocator(file)).delete();
  }

  private PhysicalFile getPhysicalFile(ContentLocator contentLocator) {
    LocalStorageDescriptor localStorage =
        findExistingStorageDescriptor(contentLocator.getStorageName());
    return physicalFileFactory.create(localStorage.getBaseDirectory(), contentLocator);
  }

  private LocalStorageDescriptor findExistingStorageDescriptor(String storageName) {
    return localStorageDescriptorRepository.findByName(storageName)
        .orElseThrow(() -> new FileStorageException(
            String.format("Local storage isn't registered", storageName)));
  }
}

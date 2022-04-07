package io.bcs.fileserver.infrastructure.storage;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.content.ContentFragment;
import io.bcs.fileserver.domain.model.content.ContentLocator;
import io.bcs.fileserver.domain.model.content.DefaultContentLocator;
import io.bcs.fileserver.domain.model.content.FileStorage;
import io.bcs.fileserver.domain.model.content.StorageType.FileStorageProvider;
import io.bcs.fileserver.domain.model.content.storage.FileContentLocator;
import io.bcs.fileserver.domain.model.content.storage.LocalStorageDescriptor;
import io.bcs.fileserver.infrastructure.storage.PhysicalFile.Factory;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the file storage, interacting with the local filesystem.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class LocalFilesystemStorageFactory implements FileStorageProvider<LocalStorageDescriptor> {
  private final FilesystemSpaceManager filesystemSpaceManager;
  private final Factory physicalFileFactory;
  private final int bufferSize;

  @Override
  public FileStorage getFileStorage(LocalStorageDescriptor storage) {
    return new LocalFilesystemStorage(storage);
  }

  @RequiredArgsConstructor
  private class LocalFilesystemStorage implements FileStorage {
    private final LocalStorageDescriptor storageDescriptor;

    @Override
    public ContentLocator create(FileDescriptor file, Long contentLength)
        throws FileStorageException {
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
        return getPhysicalFile(contentLocator).openForRead(fragment.getOffset(),
            fragment.getLength(), bufferSize);
      } catch (IOException error) {
        throw new FileStorageException(error);
      }
    }

    @Override
    public void delete(FileDescriptor file) throws FileStorageException {
      getPhysicalFile(new FileContentLocator(file)).delete();
    }

    private PhysicalFile getPhysicalFile(ContentLocator contentLocator) {
      return physicalFileFactory.create(storageDescriptor.getBaseDirectory(), contentLocator);
    }
  }
}

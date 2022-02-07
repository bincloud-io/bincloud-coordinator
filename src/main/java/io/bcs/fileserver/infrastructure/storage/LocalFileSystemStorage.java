package io.bcs.fileserver.infrastructure.storage;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptor;
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptorRepository;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the file storage, which stores.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalFileSystemStorage implements FileStorage {
  private final FilesystemSpaceManager filesystemSpaceManager;
  private final LocalStorageDescriptorRepository localStorageDescriptorRepository;
  private final int bufferSize;

  @Override
  public ContentLocator create(File file, Long contentLength) throws FileStorageException {
    try {
      String storageName = filesystemSpaceManager.allocateSpace(file.getMediaType(),
          file.getStorageFileName(), contentLength);
      ContentLocator contentLocator =
          new DefaultFileLocator(storageName, file.getStorageFileName());
      getPhysicalFile(contentLocator).createNewFile();
      return contentLocator;
    } catch (IOException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public Destination<BinaryChunk> getAccessOnWrite(File file) throws FileStorageException {
    try {
      return new OutputStreamDestination(openFileForWrite(new DefaultFileLocator(file)));
    } catch (FileNotFoundException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public Source<BinaryChunk> getAccessOnRead(File file, ContentFragment fragment)
      throws FileStorageException {
    try {
      return new InputStreamSource(openFileForRead(new DefaultFileLocator(file)), bufferSize);
    } catch (FileNotFoundException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public void delete(File file) throws FileStorageException {
    getPhysicalFile(new DefaultFileLocator(file)).delete();
  }

  private java.io.File getPhysicalFile(ContentLocator contentLocator) {
    return new java.io.File(getFileLocationPath(contentLocator));
  }
  
  private FileOutputStream openFileForWrite(ContentLocator contentLocator)
      throws FileNotFoundException {
    return new FileOutputStream(getPhysicalFile(contentLocator));
  }
  
  private FileInputStream openFileForRead(ContentLocator contentLocator)
      throws FileNotFoundException {
    return new FileInputStream(getFileLocationPath(contentLocator));
  }

  private String getFileLocationPath(ContentLocator contentLocator) {
    LocalStorageDescriptor localStorage =
        findExistingStorageDescriptor(contentLocator.getStorageName());
    return String.format("%s/%s", localStorage.getBaseDirectory(),
        contentLocator.getStorageFileName());
  }

  private LocalStorageDescriptor findExistingStorageDescriptor(String storageName) {
    return localStorageDescriptorRepository.findByName(storageName)
        .orElseThrow(() -> new FileStorageException(
            String.format("Local storage isn't registered", storageName)));
  }

  @Getter
  @EqualsAndHashCode
  @RequiredArgsConstructor
  private final class DefaultFileLocator implements ContentLocator {
    private final String storageName;
    private final String storageFileName;
    
    public DefaultFileLocator(File file) {
      super();
      this.storageFileName = file.getStorageFileName();
      this.storageName = file.getStorageName().get();
    }
  }

  /**
   * This interface describes the component which performs the free space management operations over
   * registered local storages.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface FilesystemSpaceManager {
    /**
     * Allocate space on the storage for requested length of file.
     *
     * @param mediaType       The file media type
     * @param storageFileName The storage file name
     * @param contentLength   The requested content length
     * @return The storage name on which the space will be allocated for requested file
     */
    String allocateSpace(String mediaType, String storageFileName, Long contentLength);

    /**
     * Release space on the storage for requested file.
     *
     * @param storageName     The file storage name
     * @param storageFileName The file name
     */
    void releaseSpace(String storageName, String storageFileName);

  }
}
